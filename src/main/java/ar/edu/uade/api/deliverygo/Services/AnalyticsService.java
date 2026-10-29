package ar.edu.uade.api.deliverygo.services;

import ar.edu.uade.api.deliverygo.client.PedidosClient;
import ar.edu.uade.api.deliverygo.dto.DashboardResponse;
import ar.edu.uade.api.deliverygo.dto.MetricaResumen;
import ar.edu.uade.api.deliverygo.dto.OrdersStatsDTO;
import ar.edu.uade.api.deliverygo.excepcion.ExternalServiceUnavailableException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidPeriodException;
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

        //1- validacion del periodo
        if(desde==null || hasta ==null || desde.isAfter(hasta)){
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

        }

        //4- fuente pedidos, si el módulo falla, el tablero sale
        try {
            OrdersStatsDTO stats = pedidosClient.obtenerEstadisticas("ENTREGADO", desde, hasta);
            metricas.add(guardarSnapshot(FuenteMetrica.PEDIDOS, TipoMetrica.PEDIDOS_ENTREGADOS,
                    desde, hasta, BigDecimal.valueOf(stats.getCantidad())));
        } catch (ExternalServiceUnavailableException e) {
            parcial = true;
        }

        // 5- Devolver al controller
        return new DashboardResponse(LocalDateTime.now(), metricas, parcial);

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
        return new MetricaResumen(fuente, tipo, desde, hasta, metrica.getValor());
    }



}
