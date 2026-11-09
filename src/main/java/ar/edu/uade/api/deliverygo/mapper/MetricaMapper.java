package ar.edu.uade.api.deliverygo.mapper;

import ar.edu.uade.api.deliverygo.dto.MetricaResumen;
import ar.edu.uade.api.deliverygo.model.Metrica;

// Convierte Metrica (ENT-02) al resumen de salida del tablero (DTO-10).
public final class MetricaMapper {

    private MetricaMapper() {
        // Clase de utilidad no se instancia.
    }

    public static MetricaResumen toResponse(Metrica metrica) {
        return new MetricaResumen(
                metrica.getFuente(),
                metrica.getTipoMetrica(),
                metrica.getPeriodoDesde(),
                metrica.getPeriodoHasta(),
                metrica.getValor()
        );
    }
}
