package ar.edu.uade.api.deliverygo.services;
import ar.edu.uade.api.deliverygo.dto.*;
import ar.edu.uade.api.deliverygo.excepcion.InvalidAnalyticsEventException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidPeriodException;
import ar.edu.uade.api.deliverygo.mapper.EventoAnaliticoMapper;
import ar.edu.uade.api.deliverygo.mapper.MetricaMapper;
import ar.edu.uade.api.deliverygo.model.*;
import ar.edu.uade.api.deliverygo.repository.EventoAnaliticoRepository;
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
import java.util.Objects;

/**
 * SRV-03 a SRV-07 (CU-05 a CU-10): tablero de indicadores, consulta de snapshots y registro y análisis de eventos.
 * RN-06: Cada análisis guarda su resultado como snapshot con upsert.
 * CU-07: generarTablero recalcula y guarda; consultarMetricasPorPeriodo solo lee los snapshots ya guardados.
 * DM-21: todos los indicadores salen de la tabla de reseñas propia o de eventos que envían otros módulos (API-05).
 * El tablero no consulta a ningún módulo externo.
 */
@Service
public class AnalyticsService {
    private  final ResenaRepository resenaRepository;
    private final MetricaRepository metricaRepository;
    private final EventoAnaliticoRepository eventoAnaliticoRepository;

    // Valor del campo detalle de los eventos PEDIDO_ESTADO_CAMBIADO que se cuentan en el tablero
    private static final String DETALLE_ENTREGADO = "ENTREGADO";
    private static final String DETALLE_CANCELADO = "CANCELADO";

    public AnalyticsService(ResenaRepository resenaRepository, MetricaRepository metricaRepository, EventoAnaliticoRepository eventoAnaliticoRepository) {
        this.resenaRepository = resenaRepository;
        this.metricaRepository = metricaRepository;
        this.eventoAnaliticoRepository = eventoAnaliticoRepository;
    }

    /**
     * SRV-03 CU-05: genera el tablero del período. Si desde o hasta son null, toman la fecha actual.
     * REP-09: cantidad y promedio de calificación. Pedidos entregados y cancelados (CU-08), pagos (CU-09) y entregas (CU-10):
     * se calculan a partir de los eventos recibidos (DM-21).
     */
@Transactional
public DashboardResponse generarTablero(LocalDate desde, LocalDate hasta){

    // CU-05: cada extremo omitido toma la fecha actual
    LocalDate hoy = LocalDate.now();
    desde = desde == null ? hoy : desde;
    hasta = hasta == null ? hoy : hasta;
    if (desde.isAfter(hasta)) {
        throw  new InvalidPeriodException("El periodo es invalido");
    }
    List<MetricaResumen> metricas = new ArrayList<>();

    // REP-09 (RN-03): reseñas creadas en el período. El fin es exclusivo, por eso se pasa el día siguiente a las 00:00
    List<Resena> resenas = resenaRepository
            .findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(
                    desde.atStartOfDay(),
                    hasta.plusDays(1).atStartOfDay());
    // CANTIDAD_RESENAS: siempre se guarda, aunque sea 0
    metricas.add(guardarSnapshot(FuenteMetrica.RESENAS, TipoMetrica.CANTIDAD_RESENAS,
            desde, hasta, BigDecimal.valueOf(resenas.size())));

    if (!resenas.isEmpty()) {
        int suma = resenas.stream().mapToInt(Resena::getPuntuacion).sum();
        BigDecimal promedio = BigDecimal.valueOf(suma)
                .divide(BigDecimal.valueOf(resenas.size()), 2, RoundingMode.HALF_UP);
        metricas.add(guardarSnapshot(FuenteMetrica.RESENAS, TipoMetrica.PROMEDIO_CALIFICACION,
                desde, hasta, promedio));

    } else {
        // Sin reseñas no hay promedio: no se persiste porque Metrica.valor es NOT NULL
        metricas.add(new MetricaResumen(FuenteMetrica.RESENAS, TipoMetrica.PROMEDIO_CALIFICACION,
                desde, hasta, null));
    }

    // Estas llamadas internas no pasan por el proxy de @Transactional, pero comparten la transacción de generarTablero
    // CU-08: pedidos entregados y cancelados
    metricas.add(analizarPedidosEntregados(desde, hasta));
    metricas.add(analizarPedidosCancelados(desde, hasta));

    // CU-09, DM-18: pagos aprobados y rechazados
    metricas.addAll(analizarPagos(desde, hasta));

    // CU-10, DM-18: entregas y tiempo promedio de entrega
    metricas.addAll(analizarEntregas(desde, hasta));

    return new DashboardResponse(LocalDateTime.now(), metricas);

}

/**
 * SRV-05 (CU-07): snapshots cuyo período está completamente contenido en el rango (REP-08).
 * Solo lee la tabla metricas: no recalcula ni llama a otros módulos.
 **/
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

/**
 * SRV-07 (CU-09): cuenta los pagos aprobados y rechazados del período (REP-07) y guarda un snapshot de cada uno.
 */
@Transactional
public List<MetricaResumen> analizarPagos(LocalDate periodoDesde, LocalDate periodoHasta) {
    LocalDate hoy = LocalDate.now();
    periodoDesde = periodoDesde == null ? hoy : periodoDesde;
    periodoHasta = periodoHasta == null ? hoy : periodoHasta;
    if (periodoDesde.isAfter(periodoHasta)) {
        throw new InvalidPeriodException("El periodo es invalido");
    }

    LocalDateTime desde = periodoDesde.atStartOfDay();
    LocalDateTime hasta = finDelDia(periodoHasta);
    long aprobados = eventoAnaliticoRepository
            .findByTipoEventoAndFechaEventoBetween(TipoEvento.PAGO_APROBADO, desde, hasta)
            .size();
    long rechazados = eventoAnaliticoRepository
            .findByTipoEventoAndFechaEventoBetween(TipoEvento.PAGO_RECHAZADO, desde, hasta)
            .size();

    List<MetricaResumen> metricas = new ArrayList<>();
    metricas.add(guardarSnapshot(FuenteMetrica.PAGOS, TipoMetrica.CANTIDAD_PAGOS_APROBADOS,
            periodoDesde, periodoHasta, BigDecimal.valueOf(aprobados)));
    metricas.add(guardarSnapshot(FuenteMetrica.PAGOS, TipoMetrica.CANTIDAD_PAGOS_RECHAZADOS,
            periodoDesde, periodoHasta, BigDecimal.valueOf(rechazados)));
    return metricas;
}

/**
 * SRV-08 (CU-10): cuenta las entregas completadas del período (REP-07) y calcula el tiempo promedio de entrega.
 * Solo se promedian los eventos que traen valor (minutos). Si ninguno lo trae, el promedio es null y no se persiste.
 */
@Transactional
public List<MetricaResumen> analizarEntregas(LocalDate periodoDesde, LocalDate periodoHasta) {
    LocalDate hoy = LocalDate.now();
    periodoDesde = periodoDesde == null ? hoy : periodoDesde;
    periodoHasta = periodoHasta == null ? hoy : periodoHasta;
    if (periodoDesde.isAfter(periodoHasta)) {
        throw new InvalidPeriodException("El periodo es invalido");
    }

    LocalDateTime desde = periodoDesde.atStartOfDay();
    LocalDateTime hasta = finDelDia(periodoHasta);
    List<EventoAnalitico> entregas = eventoAnaliticoRepository
            .findByTipoEventoAndFechaEventoBetween(TipoEvento.ENTREGA_COMPLETADA, desde, hasta);

    List<MetricaResumen> metricas = new ArrayList<>();
    metricas.add(guardarSnapshot(FuenteMetrica.LOGISTICA, TipoMetrica.CANTIDAD_ENTREGAS,
            periodoDesde, periodoHasta, BigDecimal.valueOf(entregas.size())));

    // Solo promedian las entregas que informan valor (minutos)
    List<BigDecimal> tiempos = entregas.stream()
            .map(EventoAnalitico::getValor)
            .filter(Objects::nonNull)
            .toList();

    if (tiempos.isEmpty()) {
        // Sin tiempos no hay promedio. No se persiste: Metrica.valor es NOT NULL.
        metricas.add(new MetricaResumen(FuenteMetrica.LOGISTICA, TipoMetrica.TIEMPO_PROMEDIO_ENTREGA,
                periodoDesde, periodoHasta, null));
    } else {
        BigDecimal suma = tiempos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedio = suma.divide(BigDecimal.valueOf(tiempos.size()), 2, RoundingMode.HALF_UP);
        metricas.add(guardarSnapshot(FuenteMetrica.LOGISTICA, TipoMetrica.TIEMPO_PROMEDIO_ENTREGA,
                periodoDesde, periodoHasta, promedio));
    }
    return metricas;
}

    /**
     * SRV-04 (CU-06): registra un evento analítico recibido de otro módulo.
     * Valida RN-09 antes de guardar. El evento se guarda crudo y se analiza después, en CU-08, CU-09 y CU-10.
     **/
@Transactional
public EventoAnaliticoResponse registrarEvento(EventoAnaliticoRequest request) {

    // RN-09
    validarEvento(request);

    // REP-06: guarda el evento
    EventoAnalitico evento = EventoAnaliticoMapper.toEntity(request);
    eventoAnaliticoRepository.save(evento);

    return EventoAnaliticoMapper.toResponse(evento);
}

// RN-09: tipoEvento, moduloOrigen, entidadId y fechaEvento son obligatorios.
// entidadId debe ser positivo y fechaEvento no puede estar en el futuro.
private void validarEvento(EventoAnaliticoRequest request) {
    List<String> faltantes = new ArrayList<>();
    if (request.getTipoEvento() == null) faltantes.add("tipoEvento");
    if (request.getModuloOrigen() == null) faltantes.add("moduloOrigen");
    if (request.getEntidadId() == null) faltantes.add("entidadId");
    if (request.getFechaEvento() == null) faltantes.add("fechaEvento");

    if (!faltantes.isEmpty()) {
        throw new InvalidAnalyticsEventException(
                "Faltan campos obligatorios del evento: " + String.join(", ", faltantes));
    }

    if (request.getEntidadId() <= 0) {
        throw new InvalidAnalyticsEventException("entidadId debe ser un número positivo");
    }

    // 1 minuto de margen: los relojes de los módulos pueden no estar perfectamente sincronizados
    if (request.getFechaEvento().isAfter(LocalDateTime.now().plusMinutes(1))) {
        throw new InvalidAnalyticsEventException("fechaEvento no puede estar en el futuro");
    }
}


/**
 * RN-06: guarda un snapshot con upsert. REP-05 busca el del mismo fuente, tipo y período: si existe, actualiza
 * valor y fechaCalculo; si no, crea uno nuevo (REP-04).
 * Dos ejecuciones simultáneas para el mismo período pueden chocar con la restricción uk_metrica_fuente_tipo_periodo,
 * y ese error no se maneja.
 */
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

/**
 * SRV-06 (CU-08): cuenta los cambios de estado a ENTREGADO del período y guarda el snapshot (RN-03, RN-06).
 * DM-21: el dato sale de los eventos PEDIDO_ESTADO_CAMBIADO que envía Pedidos, con detalle ENTREGADO.
 */
@Transactional
public MetricaResumen analizarPedidosEntregados(LocalDate periodoDesde, LocalDate periodoHasta) {
    // Sin eventos el valor es 0 y se persiste
    return guardarSnapshot(FuenteMetrica.PEDIDOS, TipoMetrica.PEDIDOS_ENTREGADOS,
            periodoDesde, periodoHasta, contarCambiosDeEstado(periodoDesde, periodoHasta, DETALLE_ENTREGADO));
}

/**
 * SRV-06 (CU-08): cuenta los cambios de estado a CANCELADO del período y guarda el snapshot (RN-03, RN-06).
 * El criterio es el texto CANCELADO en el detalle del evento, que envía Pedidos.
 */
@Transactional
public MetricaResumen analizarPedidosCancelados(LocalDate periodoDesde, LocalDate periodoHasta) {
    // Sin cancelaciones el valor es 0 y se persiste (a diferencia del promedio, que es null)
    return guardarSnapshot(FuenteMetrica.PEDIDOS, TipoMetrica.CANTIDAD_PEDIDOS_CANCELADOS,
            periodoDesde, periodoHasta, contarCambiosDeEstado(periodoDesde, periodoHasta, DETALLE_CANCELADO));
}

// REP-07 filtra por tipo y fecha, no por detalle: de todos los cambios de estado, solo cuentan los del detalle pedido
private BigDecimal contarCambiosDeEstado(LocalDate periodoDesde, LocalDate periodoHasta, String detalle) {
    long cantidad = eventoAnaliticoRepository
            .findByTipoEventoAndFechaEventoBetween(TipoEvento.PEDIDO_ESTADO_CAMBIADO,
                    periodoDesde.atStartOfDay(), finDelDia(periodoHasta))
            .stream()
            .filter(e -> detalle.equals(e.getDetalle()))
            .count();
    return BigDecimal.valueOf(cantidad);
}

// Between es inclusivo: el fin es el último instante del día con 6 decimales (la precisión de la BD), para que el día hasta entre completo
private LocalDateTime finDelDia(LocalDate fecha) {
    return fecha.atTime(23, 59, 59, 999_999_000);
}

}
