package ar.edu.uade.api.deliverygo.excepcion;

/**
 * CU-01, CU-02: el comentario supera los 1000 caracteres. Se traduce a 400 (INVALID_COMMENT).
 */
public class InvalidCommentException extends RuntimeException {

    public InvalidCommentException(String mensaje) {
        super(mensaje);
    }
}
