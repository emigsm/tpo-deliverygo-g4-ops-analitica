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
 * Traduce las excepciones de negocio a {@link ar.edu.uade.api.deliverygo.dto.ErrorResponseDTO} con su código HTTP
 * y un código estable (ej. {@code INVALID_RATING}).
 *
 * Decisión: un único punto de mapeo. Todas las respuestas de error tienen el mismo formato y un {@code correlationId}.
 * Las excepciones no declaradas acá usan el manejo por defecto de Spring.
 *
 * Mapeo:
 * <ul>
 *   <li>400: validaciones de entrada (puntuación, comentario, período, pedido, tipo, evento).</li>
 *   <li>409: reseña duplicada (RN-02).</li>
 *   <li>422: pedido inexistente o no entregado, pedido de otro usuario, usuario u objetivo inexistente o inactivo.</li>
 *   <li>503: servicio externo no disponible.</li>
 * </ul>
 */
//Mapeo de excepciones del módulo a códigos HTTP
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotDeliveredException.class)
    public ResponseEntity<ErrorResponseDTO> manejarOrderNotDelivered(
            OrderNotDeliveredException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    @ExceptionHandler(OrderNotOwnedException.class)
    public ResponseEntity<ErrorResponseDTO> manejarOrderNotOwned(
            OrderNotOwnedException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    @ExceptionHandler(ExternalEntityInvalidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarExternalEntityInvalid(
            ExternalEntityInvalidException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ErrorResponseDTO> manejarDuplicateReview(
            DuplicateReviewException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex, request);
    }
    //el error es del cliente
    @ExceptionHandler(InvalidRatingException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidRating(
            InvalidRatingException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDTO> manejarExternalServiceUnavailable(
            ExternalServiceUnavailableException ex, HttpServletRequest request) {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex, request);
    }


     //400 (BAD_REQUEST) el error es del cliente,
    @ExceptionHandler(InvalidPeriodException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidPeriod(
            InvalidPeriodException ex, HttpServletRequest request){
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(InvalidOrderIdException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidOrderId(
            InvalidOrderIdException ex, HttpServletRequest request) {

        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(InvalidAnalyticsEventException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidAnalyticsEvent(
            InvalidAnalyticsEventException ex, HttpServletRequest request){
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }


    @ExceptionHandler(InvalidTargetTypeException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidTargetType(
            InvalidTargetTypeException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(InvalidCommentException.class)
    public ResponseEntity<ErrorResponseDTO> manejarInvalidComment(
            InvalidCommentException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex, request);
    }

    /**
     * Usa el {@code X-Correlation-Id} del cliente solo si es un UUID válido. Si no, genera uno nuevo.
     * El valor inválido nunca se refleja en la respuesta.
     */
    private String correlationId(HttpServletRequest request) {
        String incoming = request.getHeader("X-Correlation-Id");
        if (incoming != null) {
            try {
                UUID uuid = UUID.fromString(incoming);
                if (uuid.toString().equalsIgnoreCase(incoming)) {
                    return incoming;
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid headers are replaced, never reflected in an error message.
            }
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Código estable para el cliente. Las excepciones que no aparecen en la lista caen en BUSINESS_RULE_VIOLATION.
     *
     * Observación: OrderNotDeliveredException, OrderNotOwnedException y ExternalEntityInvalidException devuelven
     * BUSINESS_RULE_VIOLATION por la rama por defecto. No tienen código propio.
     */
    private String codigo(RuntimeException ex) {
        if (ex instanceof DuplicateReviewException) return "DUPLICATE_REVIEW";
        if (ex instanceof InvalidRatingException) return "INVALID_RATING";
        if (ex instanceof InvalidTargetTypeException) return "INVALID_TARGET_TYPE";
        if (ex instanceof InvalidCommentException) return "INVALID_COMMENT";
        if (ex instanceof InvalidPeriodException) return "INVALID_PERIOD";
        if (ex instanceof ExternalServiceUnavailableException) return "EXTERNAL_SERVICE_UNAVAILABLE";
        if (ex instanceof InvalidOrderIdException) return "INVALID_ORDER_ID";
        if (ex instanceof InvalidAnalyticsEventException) return "INVALID_ANALYTICS_EVENT";
        return "BUSINESS_RULE_VIOLATION";
    }

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
