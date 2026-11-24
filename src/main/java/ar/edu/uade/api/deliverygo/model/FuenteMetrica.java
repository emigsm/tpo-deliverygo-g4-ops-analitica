package ar.edu.uade.api.deliverygo.model;

/**
 * RN-03: origen de los datos de una Metrica. Se guarda como texto (EnumType.STRING),
 * así que renombrar un valor rompe los datos ya guardados.
 */
public enum FuenteMetrica {
    RESENAS,
    PEDIDOS,
    PAGOS,
    LOGISTICA
}
