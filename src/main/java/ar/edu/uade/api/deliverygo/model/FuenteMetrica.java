package ar.edu.uade.api.deliverygo.model;

// Origen de datos agregados en Metrica (RN-03).

/**
 * Origen de los datos agregados en {@link Metrica} (RN-03).
 * <p>
 * Se persiste como texto ({@code EnumType.STRING}) en la columna {@code fuente}. Renombrar una
 * constante rompe los datos ya guardados, por eso los nombres se consideran estables.
 */
public enum FuenteMetrica {
    RESENAS,
    PEDIDOS,
    PAGOS,
    LOGISTICA
}
