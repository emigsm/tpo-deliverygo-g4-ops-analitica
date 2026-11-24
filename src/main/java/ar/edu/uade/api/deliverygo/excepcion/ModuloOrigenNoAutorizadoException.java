package ar.edu.uade.api.deliverygo.excepcion;

/**
 * API-05 (CU-06): el evento declara un moduloOrigen distinto al módulo dueño de la X-Api-Key usada. Se traduce a 403 (MODULE_ORIGIN_MISMATCH).
 */
public class ModuloOrigenNoAutorizadoException extends RuntimeException {
    public ModuloOrigenNoAutorizadoException(String message) {
        super(message);
    }
}
