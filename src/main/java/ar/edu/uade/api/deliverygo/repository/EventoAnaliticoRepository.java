package ar.edu.uade.api.deliverygo.repository;

import ar.edu.uade.api.deliverygo.model.EventoAnalitico;
import ar.edu.uade.api.deliverygo.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a ENT-03 ({@code eventos_analiticos}).
 * <p>
 * Hereda {@code save}, {@code findById} y {@code findAll} de {@link JpaRepository}; no hay
 * implementación escrita a mano. Spring Data genera el SQL en el arranque a partir del nombre
 * del método (derived query). Ejemplo: {@code findByTipoEventoAndFechaEventoBetween} se traduce a
 * {@code WHERE tipo_evento = ? AND fecha_evento BETWEEN ? AND ?}.
 * <p>
 * Usado por {@code AnalyticsService} en los análisis de CU-08, CU-09 y CU-10.
 */
public interface EventoAnaliticoRepository extends JpaRepository<EventoAnalitico, Long> {
    // REP-06 (CU-06, RN-09): persistir el evento -> es el save() que ya trae JpaRepository

    // REP-07 (CU-08, CU-09, CU-10): eventos de un tipo dentro de un rango de fechas.
    // "Between" es inclusivo en ambos extremos.
    List<EventoAnalitico> findByTipoEventoAndFechaEventoBetween(
            TipoEvento tipoEvento, LocalDateTime desde, LocalDateTime hasta);
}
