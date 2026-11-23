package ar.edu.uade.api.deliverygo.model;

// Tipo de métrica mostrada en el tablero (CU-05)

/**
 * Tipo de métrica que muestra el tablero (CU-05) y que se guarda en {@link Metrica#getTipoMetrica()}.
 * <p>
 * Agrupa métricas de reseñas (calificación, cantidad), pedidos, pagos y entregas. Cada valor se
 * calcula en {@code AnalyticsService} a partir de datos de su {@link FuenteMetrica}.
 */
public enum TipoMetrica {
    PROMEDIO_CALIFICACION,
    CANTIDAD_RESENAS,
    PEDIDOS_ENTREGADOS,
    CANTIDAD_PEDIDOS_CANCELADOS,
    CANTIDAD_PAGOS_APROBADOS,
    CANTIDAD_PAGOS_RECHAZADOS,
    CANTIDAD_ENTREGAS,
    TIEMPO_PROMEDIO_ENTREGA
}
