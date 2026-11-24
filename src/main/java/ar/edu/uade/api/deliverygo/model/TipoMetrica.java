package ar.edu.uade.api.deliverygo.model;

/**
 * CU-05: tipo de métrica que muestra el tablero, con su origen y su fórmula de cálculo.
 * El período es inclusivo en ambos extremos.
 */
public enum TipoMetrica {
    PROMEDIO_CALIFICACION(OrigenMetrica.RESENAS_PROPIAS,
            "Suma de las puntuaciones de las reseñas del período dividida por su cantidad, con 2 decimales. Sin reseñas es null"),
    CANTIDAD_RESENAS(OrigenMetrica.RESENAS_PROPIAS,
            "Cantidad de reseñas creadas en el período"),
    PEDIDOS_ENTREGADOS(OrigenMetrica.EVENTOS_RECIBIDOS,
            "Cantidad de eventos PEDIDO_ESTADO_CAMBIADO con detalle ENTREGADO en el período"),
    CANTIDAD_PEDIDOS_CANCELADOS(OrigenMetrica.EVENTOS_RECIBIDOS,
            "Cantidad de eventos PEDIDO_ESTADO_CAMBIADO con detalle CANCELADO en el período"),
    CANTIDAD_PAGOS_APROBADOS(OrigenMetrica.EVENTOS_RECIBIDOS,
            "Cantidad de eventos PAGO_APROBADO en el período"),
    CANTIDAD_PAGOS_RECHAZADOS(OrigenMetrica.EVENTOS_RECIBIDOS,
            "Cantidad de eventos PAGO_RECHAZADO en el período"),
    CANTIDAD_ENTREGAS(OrigenMetrica.EVENTOS_RECIBIDOS,
            "Cantidad de eventos ENTREGA_COMPLETADA en el período"),
    TIEMPO_PROMEDIO_ENTREGA(OrigenMetrica.EVENTOS_RECIBIDOS,
            "Promedio de los minutos (valor) de los eventos ENTREGA_COMPLETADA del período que informan valor, con 2 decimales. Si ninguno lo informa es null");

    private final OrigenMetrica origen;
    private final String formula;

    TipoMetrica(OrigenMetrica origen, String formula) {
        this.origen = origen;
        this.formula = formula;
    }

    public OrigenMetrica getOrigen() {
        return origen;
    }

    public String getFormula() {
        return formula;
    }
}
