package ar.edu.uade.api.deliverygo.model;

/**
 * DM-14: indica qué ocurrió en un EventoAnalitico. Cada valor alimenta un análisis:
 * PEDIDO_ESTADO_CAMBIADO (CU-08), PAGO_APROBADO y PAGO_RECHAZADO (CU-09), ENTREGA_COMPLETADA (CU-10).
 */
public enum TipoEvento {
    PEDIDO_ESTADO_CAMBIADO,
    PAGO_APROBADO,
    PAGO_RECHAZADO,
    ENTREGA_COMPLETADA
}
