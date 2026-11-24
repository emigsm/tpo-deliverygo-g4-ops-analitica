package ar.edu.uade.api.deliverygo.repository;
import ar.edu.uade.api.deliverygo.model.FuenteMetrica;
import ar.edu.uade.api.deliverygo.model.Metrica;
import ar.edu.uade.api.deliverygo.model.TipoMetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * ENT-02: acceso a la tabla metricas (snapshots ya calculados del tablero).
 * RN-06: AnalyticsService usa REP-05 para decidir si actualiza un snapshot o crea uno nuevo.
 */
public interface MetricaRepository extends JpaRepository<Metrica, Long> {
    // REP-04 (CU-05, CU-08, CU-09, RN-06): persistir el snapshot es el save() heredado de JpaRepository
    // REP-04: último snapshot de una fuente y tipo de métrica. Hoy ningún servicio lo usa
    //Optional<Metrica> findFirstByFuenteAndTipoMetricaOrderByFechaCalculoDesc(FuenteMetrica fuente, TipoMetrica tipoMetrica);
    // REP-05 (CU-05, CU-08, CU-09, RN-06): snapshot del período exacto, si existe
    Optional<Metrica> findByFuenteAndTipoMetricaAndPeriodoDesdeAndPeriodoHasta(FuenteMetrica fuente, TipoMetrica tipoMetrica, LocalDate periodoDesde, LocalDate periodoHasta);
    // REP-08 (CU-07, RN-03): snapshots completamente contenidos en el rango, límites inclusivos
    List<Metrica> findByPeriodoDesdeGreaterThanEqualAndPeriodoHastaLessThanEqual(
            LocalDate periodoDesde, LocalDate periodoHasta);
}
