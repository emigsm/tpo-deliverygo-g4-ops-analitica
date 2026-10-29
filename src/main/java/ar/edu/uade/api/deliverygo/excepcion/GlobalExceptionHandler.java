package ar.edu.uade.api.deliverygo.excepcion;

import ar.edu.uade.api.deliverygo.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

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


    private ResponseEntity<ErrorResponseDTO> construir(
            HttpStatus status, RuntimeException ex, HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }
}
