package ar.edu.uade.api.deliverygo.excepcion;

/**
 * El comentario supera los 1000 caracteres.
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 400 con código INVALID_COMMENT.
 */
public class InvalidCommentException extends RuntimeException {

    public InvalidCommentException(String mensaje) {
        super(mensaje);
    }
}
