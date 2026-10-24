package ar.edu.uade.api.deliverygo.excepcion;

//El usuario, comercio o repartidor no existe o no está activo (RN-08)
public class ExternalEntityInvalidException extends RuntimeException {
    public ExternalEntityInvalidException(String mensaje) {
        super(mensaje);
    }
}
