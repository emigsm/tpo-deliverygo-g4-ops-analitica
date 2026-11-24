package ar.edu.uade.api.deliverygo.excepcion;

/**
 * CU-05, CU-07: una fecha es nula o el período está invertido (desde posterior a hasta). Se traduce a 400 (INVALID_PERIOD).
 */
public class InvalidPeriodException extends RuntimeException {
    public InvalidPeriodException(String message) {
        super(message);
    }


}
