package ar.edu.uade.api.deliverygo.model;


// Discriminador de EventoAnalitico (DM-14): qué ocurrió
public enum TipoEvento {
    PEDIDO_ESTADO_CAMBIADO,
    PAGO_APROBADO,
    PAGO_RECHAZADO,
    ENTREGA_COMPLETADA
}
