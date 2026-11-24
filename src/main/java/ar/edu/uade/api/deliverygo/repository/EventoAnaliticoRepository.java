package ar.edu.uade.api.deliverygo.repository;

import ar.edu.uade.api.deliverygo.model.EventoAnalitico;
import ar.edu.uade.api.deliverygo.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ENT-03: acceso a la tabla eventos_analiticos. Lo usa AnalyticsService en CU-06, CU-08, CU-09 y CU-10.
 */
public interface EventoAnaliticoRepository extends JpaRepository<EventoAnalitico, Long> {
    // REP-06 (CU-06, RN-09): persistir el evento es el save() heredado de JpaRepository
    // REP-07 (CU-08, CU-09, CU-10, RN-03): eventos de un tipo dentro de un rango de fechas, ambos extremos inclusivos
    List<EventoAnalitico> findByTipoEventoAndFechaEventoBetween(
            TipoEvento tipoEvento, LocalDateTime desde, LocalDateTime hasta);
}
