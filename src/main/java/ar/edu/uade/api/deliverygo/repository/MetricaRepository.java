package ar.edu.uade.api.deliverygo.repository;

import ar.edu.uade.api.deliverygo.model.FuenteMetrica;
import ar.edu.uade.api.deliverygo.model.Metrica;
import ar.edu.uade.api.deliverygo.model.TipoMetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Acceso a ENT-02 ({@code metricas}): snapshots ya calculados del tablero.
 * <p>
 * Los nombres de los métodos derivados describen la consulta, y Spring Data la convierte en SQL
 * sin escribir JPQL. El guardado con upsert (RN-06) usa {@code findByFuenteAndTipoMetricaAndPeriodoDesdeAndPeriodoHasta}
 * desde {@code AnalyticsService}.
 */
public interface MetricaRepository extends JpaRepository<Metrica, Long> {

    // REP-08 (CU-07): snapshots completamente contenidos en el rango, límites inclusivos.
    List<Metrica> findByPeriodoDesdeGreaterThanEqualAndPeriodoHastaLessThanEqual(
            LocalDate periodoDesde, LocalDate periodoHasta);

    // REP-04: Persistir el snapshot (Default)
    // Nota: el identificador REP-04 aparece también en la consulta siguiente; revisar contra el TPO.

    // REP-04: Último snapshot persistido para una fuente/tipoMetrica dados. (Puede no haber)
    Optional<Metrica> findFirstByFuenteAndTipoMetricaOrderByFechaCalculoDesc(FuenteMetrica fuente, TipoMetrica tipoMetrica);

    // REP-05: Verificar si hay Snapshot existente para un período exacto.
    Optional<Metrica> findByFuenteAndTipoMetricaAndPeriodoDesdeAndPeriodoHasta(FuenteMetrica fuente, TipoMetrica tipoMetrica, LocalDate periodoDesde, LocalDate periodoHasta);
}
