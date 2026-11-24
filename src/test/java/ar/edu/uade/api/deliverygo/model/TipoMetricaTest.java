package ar.edu.uade.api.deliverygo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// CU-05: todo indicador del tablero tiene que poder explicarse
class TipoMetricaTest {

    @Test
    void todosLosIndicadoresTienenOrigenYFormula() {
        for (TipoMetrica tipo : TipoMetrica.values()) {
            assertNotNull(tipo.getOrigen(), tipo + " no tiene origen");
            assertFalse(tipo.getFormula().isBlank(), tipo + " no tiene fórmula");
        }
    }

    // Solo las reseñas salen de la tabla propia; el resto, de eventos que envían otros módulos
    @Test
    void soloLosIndicadoresDeResenasSalenDeLaTablaPropia() {
        assertEquals(OrigenMetrica.RESENAS_PROPIAS, TipoMetrica.CANTIDAD_RESENAS.getOrigen());
        assertEquals(OrigenMetrica.RESENAS_PROPIAS, TipoMetrica.PROMEDIO_CALIFICACION.getOrigen());
        for (TipoMetrica tipo : TipoMetrica.values()) {
            if (tipo != TipoMetrica.CANTIDAD_RESENAS && tipo != TipoMetrica.PROMEDIO_CALIFICACION) {
                assertEquals(OrigenMetrica.EVENTOS_RECIBIDOS, tipo.getOrigen(), tipo + " debería salir de eventos");
            }
        }
    }
}
