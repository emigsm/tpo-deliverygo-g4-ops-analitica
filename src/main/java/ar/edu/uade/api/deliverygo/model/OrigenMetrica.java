package ar.edu.uade.api.deliverygo.model;

/**
 * CU-05: de dónde sale el dato de una métrica.
 * RESENAS_PROPIAS: la tabla resenas de este módulo. EVENTOS_RECIBIDOS: eventos que envían otros módulos por API-05.
 */
public enum OrigenMetrica {
    RESENAS_PROPIAS,
    EVENTOS_RECIBIDOS
}
