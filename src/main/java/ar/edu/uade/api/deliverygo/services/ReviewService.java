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
import ar.edu.uade.api.deliverygo.excepcion.InvalidOrderIdException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidTargetTypeException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidCommentException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * SRV-01 y SRV-02 (CU-01 a CU-04): reglas de las reseñas y cálculo del rating.
 * Depende de interfaces (client y repository), no de implementaciones: los mocks y los clientes HTTP
 * se intercambian por perfil sin tocar esta clase (DM-11).
 */
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

    /**
     * SRV-01 (CU-01, CU-02): crea una reseña. El control de duplicado y el save van en la misma transacción.
     * Primero se validan los datos de entrada, sin llamar a otros módulos; después se consulta a Pedidos, Usuarios, Comercios o Logística.
     * DM-13: usuarioId llega como argumento desde el JWT, nunca desde el body.
     */
    @Transactional
    public ResenaResponse crearResena(ResenaRequest resenaRequest, Long usuarioIdAutenticado) {
        validarPedidoId(resenaRequest.getPedidoId());
        validarTipoObjetivo(resenaRequest.getTipoObjetivo());
        validarComentario(resenaRequest.getComentario());

        // INT-01, RN-01 (entregado): un pedido inexistente se trata como no entregado
        PedidoDTO pedido = pedidosClient.obtenerPedido(resenaRequest.getPedidoId()).orElseThrow(() -> new OrderNotDeliveredException("El pedido " + resenaRequest.getPedidoId() + " no existe"));

        if (!ESTADO_ENTREGADO.equals(pedido.getEstado())) {
            throw new OrderNotDeliveredException("El pedido " + pedido.getId() + " no fue entregado (estado actual: " + pedido.getEstado() + ")");
        }

        // RN-07: el pedido debe ser del usuario del token
        if (!usuarioIdAutenticado.equals(pedido.getUsuarioId())) {
            throw new OrderNotOwnedException("El pedido " + pedido.getId() + " no pertenece al usuario autenticado");
        }

        Long objetivoId = resolverObjetivoId(resenaRequest.getTipoObjetivo(), pedido);

        // RN-08 (INT-02, INT-03, INT-04): el usuario y el objetivo deben existir y estar activos
        validarUsuario(usuarioIdAutenticado);
        validarObjetivo(resenaRequest.getTipoObjetivo(), objetivoId);

        // RN-02 (REP-01): sin duplicado. Se controla antes que la puntuación, así que una reseña duplicada con puntuación inválida responde 409
        if (resenaRepository.existsByUsuarioIdAndPedidoIdAndTipoObjetivo(usuarioIdAutenticado, resenaRequest.getPedidoId(), resenaRequest.getTipoObjetivo())) {
            throw new DuplicateReviewException("Ya existe una reseña del usuario " + usuarioIdAutenticado + " para el pedido " + resenaRequest.getPedidoId() + " y tipo " + resenaRequest.getTipoObjetivo());
        }
        validarPuntuacion(resenaRequest.getPuntuacion());

        // REP-02: guarda la reseña
        Resena resena = ResenaMapper.toEntity(resenaRequest, usuarioIdAutenticado, objetivoId);
        resenaRepository.save(resena);

        return ResenaMapper.toResponse(resena);
    }

    // DM-13: el objetivo se deriva del pedido, no del body. Así una reseña solo apunta al comercio o repartidor que atendió ese pedido
    private Long resolverObjetivoId(TipoObjetivo tipoObjetivo, PedidoDTO pedido) {
        return switch (tipoObjetivo) {
            case COMERCIO -> pedido.getComercioId();
            case REPARTIDOR -> pedido.getRepartidorId();
        };
    }

    // RN-08 (INT-02): el usuario debe existir y estar activo
    private void validarUsuario(Long usuarioId) {
        UsuarioDTO usuario = usuariosClient.obtenerUsuario(usuarioId).orElseThrow(() -> new ExternalEntityInvalidException("El usuario " + usuarioId + " no existe"));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ExternalEntityInvalidException("El usuario " + usuarioId + " no está activo");
        }
    }

    // RN-08: el objetivo debe existir y estar activo. Consulta Comercios (INT-03) o Logística (INT-04) según el tipo
    private void validarObjetivo(TipoObjetivo tipoObjetivo, Long objetivoId) {
        if (objetivoId == null) {
            throw new ExternalEntityInvalidException("El pedido no tiene un objetivo de tipo " + tipoObjetivo);
        }
        if (tipoObjetivo == TipoObjetivo.COMERCIO) {
            ComercioDTO comercio = comercioClient.obtenerComercio(objetivoId).orElseThrow(() -> new ExternalEntityInvalidException("El comercio " + objetivoId + " no existe"));
            if (!Boolean.TRUE.equals(comercio.getActivo())) {
                throw new ExternalEntityInvalidException("El comercio " + objetivoId + " no está activo");
            }
        } else {
            RepartidorDTO repartidor = logisticaClient.obtenerRepartidor(objetivoId).orElseThrow(() -> new ExternalEntityInvalidException("El repartidor " + objetivoId + " no existe"));
            if (!Boolean.TRUE.equals(repartidor.getActivo())) {
                throw new ExternalEntityInvalidException("El repartidor " + objetivoId + " no está activo");
            }
        }
    }

    // CU-01, CU-02: el id del pedido es obligatorio y positivo
    private void validarPedidoId(Long pedidoId) {
        if (pedidoId == null || pedidoId <= 0) {
            throw new InvalidOrderIdException("El identificador del pedido es obligatorio y debe ser mayor que cero");
        }
    }

    // CU-01, CU-02: el tipo de objetivo es obligatorio
    private void validarTipoObjetivo(TipoObjetivo tipoObjetivo) {
        if (tipoObjetivo == null) {
            throw new InvalidTargetTypeException("El tipo de objetivo es obligatorio");
        }
    }

    // CU-01, CU-02: el comentario es opcional, con un máximo de 1000 caracteres
    private void validarComentario(String comentario) {
        if (comentario != null && comentario.length() > 1000) {
            throw new InvalidCommentException("El comentario no puede superar los 1000 caracteres");
        }
    }

    // RN-04: la puntuación va de 1 a 5
    private void validarPuntuacion(Integer puntuacion) {
        if (puntuacion == null || puntuacion < 1 || puntuacion > 5) {
            throw new InvalidRatingException("La puntuación debe estar entre 1 y 5");
        }
    }

    /**
     * SRV-02 (CU-03, CU-04): promedio de calificación de un comercio o repartidor, con 2 decimales (HALF_UP).
     * DM-12: sin reseñas devuelve promedio null y cantidad 0.
     */
    @Transactional(readOnly = true)
    public RatingResponse obtenerPromedio(TipoObjetivo tipoObjetivo, Long objetivoId) {

        // REP-03: trae las reseñas del objetivo y calcula el promedio en memoria
        List<Resena> resenas = resenaRepository.findByObjetivoIdAndTipoObjetivo(objetivoId, tipoObjetivo);

        if (resenas.isEmpty()) {
            // DM-12: 200 con cantidadResenas 0 y promedio null, no 404
            return new RatingResponse(objetivoId, tipoObjetivo, null, 0L);
        }

        // Se suman enteros y el total se pasa a BigDecimal recién al final
        int sumaInt = resenas.stream().mapToInt(Resena::getPuntuacion).sum();
        BigDecimal suma = BigDecimal.valueOf(sumaInt);

        // Promedio con 2 decimales, redondeo HALF_UP
        BigDecimal promedio = suma.divide(BigDecimal.valueOf(resenas.size()), 2, RoundingMode.HALF_UP);

        return new RatingResponse(objetivoId, tipoObjetivo, promedio, (long) resenas.size());
    }
}
