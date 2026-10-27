package ar.edu.uade.api.deliverygo.services;

import ar.edu.uade.api.deliverygo.client.ComercioClient;
import ar.edu.uade.api.deliverygo.client.LogisticaClient;
import ar.edu.uade.api.deliverygo.client.PedidosClient;
import ar.edu.uade.api.deliverygo.client.UsuariosClient;
import ar.edu.uade.api.deliverygo.dto.ComercioDTO;
import ar.edu.uade.api.deliverygo.dto.PedidoDTO;
import ar.edu.uade.api.deliverygo.dto.RatingResponse;
import ar.edu.uade.api.deliverygo.dto.RepartidorDTO;
import ar.edu.uade.api.deliverygo.dto.ResenaRequest;
import ar.edu.uade.api.deliverygo.dto.ResenaResponse;
import ar.edu.uade.api.deliverygo.dto.UsuarioDTO;
import ar.edu.uade.api.deliverygo.excepcion.DuplicateReviewException;
import ar.edu.uade.api.deliverygo.excepcion.ExternalEntityInvalidException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidRatingException;
import ar.edu.uade.api.deliverygo.excepcion.OrderNotDeliveredException;
import ar.edu.uade.api.deliverygo.excepcion.OrderNotOwnedException;
import ar.edu.uade.api.deliverygo.model.Resena;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.repository.ResenaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ar.edu.uade.api.deliverygo.mapper.ResenaMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

//SRV-01, SRV-02 (CU-01 a CU-04)
@Service
public class ReviewService {

    private static final String ESTADO_ENTREGADO = "ENTREGADO";

    private final ResenaRepository resenaRepository;
    private final PedidosClient pedidosClient;
    private final UsuariosClient usuariosClient;
    private final ComercioClient comercioClient;
    private final LogisticaClient logisticaClient;

    public ReviewService(ResenaRepository resenaRepository, PedidosClient pedidosClient, UsuariosClient usuariosClient, ComercioClient comercioClient, LogisticaClient logisticaClient) {
        this.resenaRepository = resenaRepository;
        this.pedidosClient = pedidosClient;
        this.usuariosClient = usuariosClient;
        this.comercioClient = comercioClient;
        this.logisticaClient = logisticaClient;
    }

    // SRV-01 (CU-01, CU-02)
    @Transactional
    public ResenaResponse crearResena(ResenaRequest resenaRequest, Long usuarioIdAutenticado) {

        //INT-01, RN-01 (entregado) y RN-07 (dueño); objetivoId sale del PedidoDTO
        PedidoDTO pedido = pedidosClient.obtenerPedido(resenaRequest.getPedidoId()).orElseThrow(() -> new OrderNotDeliveredException("El pedido " + resenaRequest.getPedidoId() + " no existe"));

        if (!ESTADO_ENTREGADO.equals(pedido.getEstado())) {
            throw new OrderNotDeliveredException("El pedido " + pedido.getId() + " no fue entregado (estado actual: " + pedido.getEstado() + ")");
        }

        if (!pedido.getUsuarioId().equals(usuarioIdAutenticado)) {
            throw new OrderNotOwnedException("El pedido " + pedido.getId() + " no pertenece al usuario autenticado");
        }

        Long objetivoId = resolverObjetivoId(resenaRequest.getTipoObjetivo(), pedido);

        // INT-02 e INT-03/INT-04, RN-08
        validarUsuario(usuarioIdAutenticado);
        validarObjetivo(resenaRequest.getTipoObjetivo(), objetivoId);

        // RN-02 (sin duplicado) y RN-04 (puntuación 1-5)
        if (resenaRepository.existsByUsuarioIdAndPedidoIdAndTipoObjetivo(usuarioIdAutenticado, resenaRequest.getPedidoId(), resenaRequest.getTipoObjetivo())) {
            throw new DuplicateReviewException("Ya existe una reseña del usuario " + usuarioIdAutenticado + " para el pedido " + resenaRequest.getPedidoId() + " y tipo " + resenaRequest.getTipoObjetivo());
        }
        validarPuntuacion(resenaRequest.getPuntuacion());

        // Guardar Reseña
        Resena resena = ResenaMapper.toEntity(resenaRequest, usuarioIdAutenticado, objetivoId);
        resenaRepository.save(resena);

        // Devolver a controller
        return ResenaMapper.toResponse(resena);
    }

    //Obtener ID según el tipoObjetivo
    private Long resolverObjetivoId(TipoObjetivo tipoObjetivo, PedidoDTO pedido) {
        return switch (tipoObjetivo) {
            case COMERCIO -> pedido.getComercioId();
            case REPARTIDOR -> pedido.getRepartidorId();
        };
    }

    private void validarUsuario(Long usuarioId) {
        // Validar si existe el usuario
        UsuarioDTO usuario = usuariosClient.obtenerUsuario(usuarioId).orElseThrow(() -> new ExternalEntityInvalidException("El usuario " + usuarioId + " no existe"));
        // Validar si el usuario está activo
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ExternalEntityInvalidException("El usuario " + usuarioId + " no está activo");
        }
    }

    private void validarObjetivo(TipoObjetivo tipoObjetivo, Long objetivoId) {
        if (tipoObjetivo == TipoObjetivo.COMERCIO) {
            //validar que existe el comercio
            ComercioDTO comercio = comercioClient.obtenerComercio(objetivoId).orElseThrow(() -> new ExternalEntityInvalidException("El comercio " + objetivoId + " no existe"));
            //validar que el comercio esté activo
            if (!Boolean.TRUE.equals(comercio.getActivo())) {
                throw new ExternalEntityInvalidException("El comercio " + objetivoId + " no está activo");
            }
        } else {
            //validar que el repartidor exista
            RepartidorDTO repartidor = logisticaClient.obtenerRepartidor(objetivoId).orElseThrow(() -> new ExternalEntityInvalidException("El repartidor " + objetivoId + " no existe"));
            //validar que el repartidor esté activo
            if (!Boolean.TRUE.equals(repartidor.getActivo())) {
                throw new ExternalEntityInvalidException("El repartidor " + objetivoId + " no está activo");
            }
        }
    }

    private void validarPuntuacion(Integer puntuacion) {
        //validar que le puntuación esté entre el 1 y el 5
        if (puntuacion == null || puntuacion < 1 || puntuacion > 5) {
            throw new InvalidRatingException("La puntuación debe estar entre 1 y 5");
        }
    }

    // SRV-02 (CU-03, CU-04)
    @Transactional(readOnly = true)
    public RatingResponse obtenerPromedio(TipoObjetivo tipoObjetivo, Long objetivoId) {

        List<Resena> resenas = resenaRepository.findByObjetivoIdAndTipoObjetivo(objetivoId, tipoObjetivo);

        if (resenas.isEmpty()) {
            // DM-12 -- devolver 200 con cantidadResenas 0 y promedio null, no 404
            return new RatingResponse(objetivoId, tipoObjetivo, null, 0L);
        }

        // Suma las puntuaciones como enteros y convierte el total a BigDecimal recién al final.
        int sumaInt = resenas.stream().mapToInt(Resena::getPuntuacion).sum();
        BigDecimal suma = BigDecimal.valueOf(sumaInt);

        //Calcula el promedio con 2 decimales de precisión, redondeando de forma estándar
        BigDecimal promedio = suma.divide(BigDecimal.valueOf(resenas.size()), 2, RoundingMode.HALF_UP);

        return new RatingResponse(objetivoId, tipoObjetivo, promedio, (long) resenas.size());
    }
}
