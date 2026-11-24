package ar.edu.uade.api.deliverygo.model;

/**
 * DM-15: módulo que emitió un EventoAnalitico. Enum cerrado: solo estos tres se analizan en CU-08, CU-09 y CU-10.
 */
public enum ModuloOrigen {
    PEDIDOS,
    PAGOS,
    LOGISTICA
}
