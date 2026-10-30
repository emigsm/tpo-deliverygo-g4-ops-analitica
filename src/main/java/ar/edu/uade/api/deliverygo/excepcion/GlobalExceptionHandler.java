package ar.edu.uade.api.deliverygo.excepcion;

import ar.edu.uade.api.deliverygo.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;

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

    private String codigo(RuntimeException ex) {
        if (ex instanceof DuplicateReviewException) return "DUPLICATE_REVIEW";
        if (ex instanceof InvalidRatingException) return "INVALID_RATING";
        if (ex instanceof InvalidPeriodException) return "INVALID_PERIOD";
        if (ex instanceof ExternalServiceUnavailableException) return "EXTERNAL_SERVICE_UNAVAILABLE";
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
