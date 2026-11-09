package ar.edu.uade.api.deliverygo.services;

import ar.edu.uade.api.deliverygo.client.PedidosClient;
import ar.edu.uade.api.deliverygo.dto.DashboardResponse;
import ar.edu.uade.api.deliverygo.dto.MetricaResumen;
import ar.edu.uade.api.deliverygo.dto.OrdersStatsDTO;
import ar.edu.uade.api.deliverygo.excepcion.ExternalServiceUnavailableException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidPeriodException;
import ar.edu.uade.api.deliverygo.mapper.MetricaMapper;
import ar.edu.uade.api.deliverygo.model.FuenteMetrica;
import ar.edu.uade.api.deliverygo.model.Metrica;
import ar.edu.uade.api.deliverygo.model.Resena;
import ar.edu.uade.api.deliverygo.model.TipoMetrica;
import ar.edu.uade.api.deliverygo.repository.MetricaRepository;
import ar.edu.uade.api.deliverygo.repository.ResenaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class AnalyticsService {
    private  final ResenaRepository resenaRepository;
    private final MetricaRepository metricaRepository;
    private final PedidosClient pedidosClient;

    public AnalyticsService(ResenaRepository resenaRepository, MetricaRepository metricaRepository, PedidosClient pedidosClient) {
        this.resenaRepository = resenaRepository;
        this.metricaRepository = metricaRepository;
        this.pedidosClient = pedidosClient;
    }

    @Transactional
    public DashboardResponse generarTablero(LocalDate desde, LocalDate hasta){

        // CU-05: cada extremo omitido toma la fecha actual del servidor.
        LocalDate hoy = LocalDate.now();
        desde = desde == null ? hoy : desde;
        hasta = hasta == null ? hoy : hasta;
        if (desde.isAfter(hasta)) {
            throw  new InvalidPeriodException("El periodo es invalido");
        }
        List<MetricaResumen> metricas = new ArrayList<>();
        boolean parcial = false;

        //2- fuente resenas
        List<Resena> resenas = resenaRepository
                .findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(
                        desde.atStartOfDay(),
                        hasta.plusDays(1).atStartOfDay());
        //3- calcular y guardar metrica
        metricas.add(guardarSnapshot(FuenteMetrica.RESENAS, TipoMetrica.CANTIDAD_RESENAS,
                desde, hasta, BigDecimal.valueOf(resenas.size())));

        if (!resenas.isEmpty()) {
            int suma = resenas.stream().mapToInt(Resena::getPuntuacion).sum();
            BigDecimal promedio = BigDecimal.valueOf(suma)
                    .divide(BigDecimal.valueOf(resenas.size()), 2, RoundingMode.HALF_UP);
            metricas.add(guardarSnapshot(FuenteMetrica.RESENAS, TipoMetrica.PROMEDIO_CALIFICACION,
                    desde, hasta, promedio));

        } else {
            // Un promedio sin observaciones no se persiste: Metrica.valor es NOT NULL.
            metricas.add(new MetricaResumen(FuenteMetrica.RESENAS, TipoMetrica.PROMEDIO_CALIFICACION,
                    desde, hasta, null));
        }

        // INT-05 (propuesto): consumir exclusivamente estadísticas agregadas.
        try {
            OrdersStatsDTO stats = pedidosClient.obtenerEstadisticas("ENTREGADO", desde, hasta);
            if (stats == null || stats.getCantidad() == null || stats.getCantidad() < 0
                    || !"ENTREGADO".equals(stats.getEstado())
                    || !desde.equals(stats.getPeriodoDesde()) || !hasta.equals(stats.getPeriodoHasta())) {
                throw new ExternalServiceUnavailableException("Pedidos devolvió estadísticas inválidas");
            }
            metricas.add(guardarSnapshot(FuenteMetrica.PEDIDOS, TipoMetrica.PEDIDOS_ENTREGADOS,
                    desde, hasta, BigDecimal.valueOf(stats.getCantidad())));
        } catch (ExternalServiceUnavailableException e) {
            // RN-03/RN-06: únicamente aplica el snapshot de la misma fuente, tipo y período.
            Metrica snapshot = metricaRepository
                    .findByFuenteAndTipoMetricaAndPeriodoDesdeAndPeriodoHasta(
                            FuenteMetrica.PEDIDOS, TipoMetrica.PEDIDOS_ENTREGADOS, desde, hasta)
                    .filter(metrica -> metrica.getValor() != null && metrica.getValor().signum() >= 0)
                    .orElseThrow(() -> new ExternalServiceUnavailableException(
                            "Pedidos no disponible y no existe un snapshot válido para el período solicitado"));
            metricas.add(MetricaMapper.toResponse(snapshot));
            parcial = true;
        }

        // 5- Devolver al controller
        return new DashboardResponse(LocalDateTime.now(), metricas, parcial);

    }

    // SRV-05 / REP-08 (CU-07): solo lee snapshots; no recalcula ni consulta proveedores.
    @Transactional(readOnly = true)
    public List<MetricaResumen> consultarMetricasPorPeriodo(LocalDate periodoDesde, LocalDate periodoHasta) {
        if (periodoDesde == null || periodoHasta == null || periodoDesde.isAfter(periodoHasta)) {
            throw new InvalidPeriodException("Ambas fechas son obligatorias y periodoDesde debe ser igual o anterior a periodoHasta");
        }
        return metricaRepository
                .findByPeriodoDesdeGreaterThanEqualAndPeriodoHastaLessThanEqual(periodoDesde, periodoHasta)
                .stream()
                .map(MetricaMapper::toResponse)
                .toList();
    }

    // RN-06: upsert. REP-05 busca el snapshot del período exacto y REP-04 lo persiste.
    private MetricaResumen guardarSnapshot(FuenteMetrica fuente, TipoMetrica tipo,
                                           LocalDate desde, LocalDate hasta, BigDecimal valor) {
        Metrica metrica = metricaRepository
                .findByFuenteAndTipoMetricaAndPeriodoDesdeAndPeriodoHasta(fuente, tipo, desde, hasta)
                .map(existente -> {                      // ya existe: se actualiza, no se duplica
                    existente.setValor(valor);
                    existente.setFechaCalculo(LocalDateTime.now());
                    return existente;
                })
                .orElseGet(() -> new Metrica(fuente, tipo, desde, hasta, valor, LocalDateTime.now()));

        metricaRepository.save(metrica);
        return MetricaMapper.toResponse(metrica);
    }



}
