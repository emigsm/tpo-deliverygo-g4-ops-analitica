package ar.edu.uade.api.deliverygo.excepcion;

/**
 * API-05, RN-09: Un evento declara un moduloOrigen distinto al módulo dueño de la X-Api-Key usada, resulta en 403 con código MODULE_ORIGIN_MISMATCH.
 */
public class ModuloOrigenNoAutorizadoException extends RuntimeException {
    public ModuloOrigenNoAutorizadoException(String message) {
        super(message);
    }
}
