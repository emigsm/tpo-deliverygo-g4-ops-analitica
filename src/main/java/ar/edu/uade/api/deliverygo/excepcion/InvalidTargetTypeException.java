package ar.edu.uade.api.deliverygo.excepcion;

/**
 * CU-01, CU-02: falta el tipo de objetivo (COMERCIO o REPARTIDOR). Se traduce a 400 (INVALID_TARGET_TYPE).
 */
public class InvalidTargetTypeException extends RuntimeException {

    public InvalidTargetTypeException(String mensaje) {
        super(mensaje);
    }
}
