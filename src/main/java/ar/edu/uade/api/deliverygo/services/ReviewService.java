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
 * Servicio de reseñas (SRV-01, SRV-02; CU-01 a CU-04).
 *
 * Orquesta las validaciones de negocio antes de persistir: pedido entregado y propio (INT-01),
 * usuario y objetivo activos (INT-02, INT-03, INT-04), reseña no duplicada (RN-02) y puntuación 1 a 5 (RN-04).
 *
 * Depende de interfaces ({@code client.*} y {@code repository.*}), no de implementaciones. Por eso los
 * mocks y los clientes HTTP se intercambian por perfil sin tocar esta clase.
 */
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

    /**
     * Crea una reseña (SRV-01, CU-01 y CU-02).
     *
     * {@code @Transactional}: la verificación de duplicado y el {@code save} ocurren en la misma transacción.
     * Las validaciones de formato van primero (baratas, sin llamadas externas). Las llamadas a otros módulos van después.
     *
     * Decisión: {@code usuarioId} llega como argumento desde la identidad autenticada, nunca desde el body (DM-13).
     *
     * @param resenaRequest datos de la reseña (DTO-01)
     * @param usuarioIdAutenticado usuario autenticado que crea la reseña
     * @return reseña persistida (DTO-02)
     * @throws OrderNotDeliveredException si el pedido no existe o no está ENTREGADO
     * @throws OrderNotOwnedException si el pedido es de otro usuario
     * @throws DuplicateReviewException si ya existe una reseña para usuario, pedido y tipo (RN-02)
     * @throws InvalidRatingException si la puntuación no está entre 1 y 5 (RN-04)
     */
    // SRV-01 (CU-01, CU-02)
    @Transactional
    public ResenaResponse crearResena(ResenaRequest resenaRequest, Long usuarioIdAutenticado) {
        validarPedidoId(resenaRequest.getPedidoId());
        validarTipoObjetivo(resenaRequest.getTipoObjetivo());
        validarComentario(resenaRequest.getComentario());

        // Observación: un pedido inexistente lanza OrderNotDeliveredException. El tipo de error es discutible; ver guía de observaciones.
        //INT-01, RN-01 (entregado) y RN-07 (dueño); objetivoId sale del PedidoDTO
        PedidoDTO pedido = pedidosClient.obtenerPedido(resenaRequest.getPedidoId()).orElseThrow(() -> new OrderNotDeliveredException("El pedido " + resenaRequest.getPedidoId() + " no existe"));

        if (!ESTADO_ENTREGADO.equals(pedido.getEstado())) {
            throw new OrderNotDeliveredException("El pedido " + pedido.getId() + " no fue entregado (estado actual: " + pedido.getEstado() + ")");
        }

        // Observación: si pedido.getUsuarioId() fuera null, esta comparación lanzaría NullPointerException.
        if (!pedido.getUsuarioId().equals(usuarioIdAutenticado)) {
            throw new OrderNotOwnedException("El pedido " + pedido.getId() + " no pertenece al usuario autenticado");
        }

        Long objetivoId = resolverObjetivoId(resenaRequest.getTipoObjetivo(), pedido);

        // INT-02 e INT-03/INT-04, RN-08
        validarUsuario(usuarioIdAutenticado);
        validarObjetivo(resenaRequest.getTipoObjetivo(), objetivoId);

        // Decisión: la puntuación se valida después del control de duplicado. Una reseña duplicada con puntuación inválida responde DuplicateReview.
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

    // Decisión: el objetivo se deriva del pedido (no del body). Así una reseña solo puede apuntar al comercio o repartidor que atendió ese pedido.
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

    // Valida que el objetivo exista y esté activo. Según el tipo, consulta Comercios (INT-03) o Logística (INT-04).
    private void validarObjetivo(TipoObjetivo tipoObjetivo, Long objetivoId) {
        if (objetivoId == null) {
            throw new ExternalEntityInvalidException("El pedido no tiene un objetivo de tipo " + tipoObjetivo);
        }
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

    private void validarPedidoId(Long pedidoId) {
    if (pedidoId == null || pedidoId <= 0) {
        throw new InvalidOrderIdException(
                "El identificador del pedido es obligatorio y debe ser mayor que cero"
        );
    }
    }

    private void validarTipoObjetivo(TipoObjetivo tipoObjetivo) {
        if (tipoObjetivo == null) {
            throw new InvalidTargetTypeException("El tipo de objetivo es obligatorio");
        }
    }

    private void validarComentario(String comentario) {
        if (comentario != null && comentario.length() > 1000) {
            throw new InvalidCommentException("El comentario no puede superar los 1000 caracteres");
        }
    }

    private void validarPuntuacion(Integer puntuacion) {
        //validar que le puntuación esté entre el 1 y el 5
        if (puntuacion == null || puntuacion < 1 || puntuacion > 5) {
            throw new InvalidRatingException("La puntuación debe estar entre 1 y 5");
        }
    }

    /**
     * Calcula el promedio de calificación de un comercio o repartidor (SRV-02; CU-03 y CU-04).
     *
     * {@code readOnly = true}: el método no escribe, así que la BD puede optimizar la transacción.
     *
     * @return promedio con 2 decimales (HALF_UP) y cantidad de reseñas. Sin reseñas, promedio {@code null} y cantidad 0 (DM-12).
     */
    // SRV-02 (CU-03, CU-04)
    @Transactional(readOnly = true)
    public RatingResponse obtenerPromedio(TipoObjetivo tipoObjetivo, Long objetivoId) {

        // Observación: carga todas las reseñas en memoria. Con volúmenes grandes, una consulta AVG/COUNT en el repositorio
        // sería más eficiente. Es una alternativa a evaluar, no un defecto confirmado.
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
