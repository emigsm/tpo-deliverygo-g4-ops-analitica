package ar.edu.uade.api.deliverygo.excepcion;

/**
 * RN-07 (CU-01, CU-02): el pedido pertenece a otro usuario. Se traduce a 422 (BUSINESS_RULE_VIOLATION).
 * Se usa 422 y no 403, como está documentado en OpenAPI.
 */
public class OrderNotOwnedException extends RuntimeException {
    public OrderNotOwnedException(String mensaje) {
        super(mensaje);
    }
}
