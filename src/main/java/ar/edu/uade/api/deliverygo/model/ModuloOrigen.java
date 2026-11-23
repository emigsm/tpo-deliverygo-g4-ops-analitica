package ar.edu.uade.api.deliverygo.model;

// Módulo que emitió el evento (DM-15). Enum cerrado: solo estos tres se analizan en CU-08/09/10
/**
 * Módulo que emitió un {@link EventoAnalitico} (DM-15).
 * <p>
 * Es un enum cerrado: solo estos tres módulos se analizan en CU-08, CU-09 y CU-10. Agregar un
 * módulo nuevo requiere cambiar el enum y el análisis correspondiente.
 */
public enum ModuloOrigen {
    PEDIDOS,
    PAGOS,
    LOGISTICA
}
