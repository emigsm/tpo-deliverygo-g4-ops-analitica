package ar.edu.uade.api.deliverygo.excepcion;

/**
 * RN-01 (CU-01, CU-02): el pedido no existe o no está ENTREGADO. Se traduce a 422 (BUSINESS_RULE_VIOLATION).
 * Un pedido inexistente se trata como no entregado.
 */
public class OrderNotDeliveredException extends RuntimeException {
    public OrderNotDeliveredException(String mensaje) {
        super(mensaje);
    }
}
