package ar.edu.uade.api.deliverygo.model;

// Tipo de Reseña (DM-01).

/**
 * Tipo de objetivo de una {@link Resena} (DM-01): comercio o repartidor.
 * <p>
 * Junto con {@code objetivoId} identifica a quién se calificó. Ambos campos forman parte de la
 * regla RN-02, que permite calificar al comercio y al repartidor del mismo pedido.
 */
public enum TipoObjetivo {
    COMERCIO,
    REPARTIDOR
}
