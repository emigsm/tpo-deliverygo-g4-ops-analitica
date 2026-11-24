package ar.edu.uade.api.deliverygo.mapper;

import ar.edu.uade.api.deliverygo.dto.MetricaResumen;
import ar.edu.uade.api.deliverygo.model.Metrica;

/**
 * ENT-02 -> DTO-10 (CU-05): omite id y fechaCalculo, que el tablero no expone.
 */
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
