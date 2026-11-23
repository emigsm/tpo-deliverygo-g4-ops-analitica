package ar.edu.uade.api.deliverygo.model;


// Discriminador de EventoAnalitico (DM-14): qué ocurrió
/**
 * Discriminador de {@link EventoAnalitico} (DM-14): indica qué ocurrió.
 * <p>
 * Cada constante alimenta un análisis: pagos aprobados/rechazados (CU-09), entregas (CU-10)
 * y cambios de estado de pedido (CU-08). Se persiste como texto.
 */
public enum TipoEvento {
    PEDIDO_ESTADO_CAMBIADO,
    PAGO_APROBADO,
    PAGO_RECHAZADO,
    ENTREGA_COMPLETADA
}
