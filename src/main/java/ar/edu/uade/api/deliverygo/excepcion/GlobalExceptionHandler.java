package ar.edu.uade.api.deliverygo.excepcion;
import ar.edu.uade.api.deliverygo.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.UUID;

/**
 * Traduce las excepciones de negocio a ErrorResponseDTO, con su código HTTP y un code estable.
 * Todas extienden RuntimeException: si se lanzan dentro de un @Transactional, se revierte la transacción.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 BAD_REQUEST: el error es del cliente
    // RN-04 (CU-01, CU-02): la puntuación no está entre 1 y 5
    @ExceptionHandler(InvalidRatingException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidRating(
            InvalidRatingException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    // CU-01, CU-02: el comentario supera el largo máximo
    @ExceptionHandler(InvalidCommentException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidComment(
            InvalidCommentException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    // CU-01, CU-02: el tipo de objetivo (COMERCIO o REPARTIDOR) no fue informado
    @ExceptionHandler(InvalidTargetTypeException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidTargetType(
            InvalidTargetTypeException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    // CU-01, CU-02: el id del pedido es nulo o no es positivo
    @ExceptionHandler(InvalidOrderIdException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidOrderId(
            InvalidOrderIdException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    // CU-05, CU-07: el período de fechas es inválido (por ejemplo, desde posterior a hasta)
    @ExceptionHandler(InvalidPeriodException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidPeriod(
            InvalidPeriodException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    // RN-09 (CU-06): evento con campos faltantes, entidadId no positivo o fechaEvento futura
    @ExceptionHandler(InvalidAnalyticsEventException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidAnalyticsEvent(
            InvalidAnalyticsEventException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    // 403 FORBIDDEN: se sabe quién es, pero no tiene permiso
    // API-05 (CU-06): la X-Api-Key es de un módulo distinto al declarado en moduloOrigen
    @ExceptionHandler(ModuloOrigenNoAutorizadoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarModuloOrigenNoAutorizado(
            ModuloOrigenNoAutorizadoException ex, HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, ex, request);
    }

    // 409 CONFLICT: choca con algo que ya existe
    // RN-02 (CU-01, CU-02): ya existe una reseña del usuario para ese pedido y tipo de objetivo
    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ErrorResponseDTO> manejarDuplicateReview(
            DuplicateReviewException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex, request);
    }

    // 422 UNPROCESSABLE_ENTITY: el pedido es válido pero no cumple una regla de negocio
    // RN-01 (CU-01, CU-02): el pedido no existe o no está ENTREGADO
    @ExceptionHandler(OrderNotDeliveredException.class)
    public ResponseEntity<ErrorResponseDTO> manejarOrderNotDelivered(
            OrderNotDeliveredException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    // RN-07 (CU-01, CU-02): el pedido pertenece a otro usuario
    @ExceptionHandler(OrderNotOwnedException.class)
    public ResponseEntity<ErrorResponseDTO> manejarOrderNotOwned(
            OrderNotOwnedException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    // RN-08 (CU-01, CU-02): el usuario, comercio o repartidor no existe o está inactivo
    @ExceptionHandler(ExternalEntityInvalidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarExternalEntityInvalid(
            ExternalEntityInvalidException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    // 503 SERVICE_UNAVAILABLE: falla un módulo externo
    // INT-01 a INT-04: el módulo externo no responde o devolvió un dato inválido
    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDTO> manejarExternalServiceUnavailable(
            ExternalServiceUnavailableException ex, HttpServletRequest request) {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex, request);
    }

    // número de seguimiento para cada error lanzado
    public static String correlationId(HttpServletRequest request) {
        String incoming = request.getHeader("X-Correlation-Id");

        if (incoming != null) {//si el header llego vacio, sigo de largo
            try {
                UUID uuid = UUID.fromString(incoming);//pruebo de convertir el string a un UUID
                if (uuid.toString().equalsIgnoreCase(incoming)) {//lo limpio y retorno
                    return incoming;
                }
            } catch (IllegalArgumentException ignored) {
                // Si el texto era basura, el catch lo atrapa y no hace nada. No muestro el mensaje para no filtrar datos
            }
        }
        return UUID.randomUUID().toString();
    }

    // Recibe la excepción y devuelve el código que le corresponde:
    // Estándarizo el mensaje de error para que si es de tal tipo de excepción, el mensaje de error sea siempre el mismo.
    private String codigo(RuntimeException ex) {
        if (ex instanceof DuplicateReviewException) return "DUPLICATE_REVIEW";
        if (ex instanceof InvalidRatingException) return "INVALID_RATING";
        if (ex instanceof InvalidTargetTypeException) return "INVALID_TARGET_TYPE";
        if (ex instanceof InvalidCommentException) return "INVALID_COMMENT";
        if (ex instanceof InvalidPeriodException) return "INVALID_PERIOD";
        if (ex instanceof ExternalServiceUnavailableException) return "EXTERNAL_SERVICE_UNAVAILABLE";
        if (ex instanceof InvalidOrderIdException) return "INVALID_ORDER_ID";
        if (ex instanceof InvalidAnalyticsEventException) return "INVALID_ANALYTICS_EVENT";
        if (ex instanceof ModuloOrigenNoAutorizadoException) return "MODULE_ORIGIN_MISMATCH";
        return "BUSINESS_RULE_VIOLATION";
    }

    // Arma el ErrorResponseDTO (fecha, status, código, mensaje, ruta y correlationId) y lo devuelve con el status HTTP
    private ResponseEntity<ErrorResponseDTO> construir(
            HttpStatus status, RuntimeException ex, HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                Instant.now(),
                status.value(),
                codigo(ex),
                ex.getMessage(),
                request.getRequestURI(),
                correlationId(request)
        );

        return ResponseEntity.status(status).body(error);
    }
}
