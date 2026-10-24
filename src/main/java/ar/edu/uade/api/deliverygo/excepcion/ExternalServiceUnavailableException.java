package ar.edu.uade.api.deliverygo.excepcion;

// Se utiliza cuando un módulo externo no responde (503)
public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String mensaje) {
        super(mensaje);
    }
}
