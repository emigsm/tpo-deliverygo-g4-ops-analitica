package ar.edu.uade.api.deliverygo.services;

import ar.edu.uade.api.deliverygo.client.PedidosClient;
import ar.edu.uade.api.deliverygo.dto.*;
import ar.edu.uade.api.deliverygo.excepcion.ExternalServiceUnavailableException;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Servicio de analítica: genera el tablero (CU-05), consulta snapshots (CU-07) y analiza eventos de pedidos,
 * pagos y entregas (CU-08, CU-09, CU-10).
 *
 * Patrón central: cada análisis <em>calcula</em> a partir de datos de origen y <em>persiste</em> el resultado como
 * {@link ar.edu.uade.api.deliverygo.model.Metrica} con upsert (RN-06, método {@code guardarSnapshot}). El tablero
 * y las consultas leen esos snapshots, no recalculan.
 *
 * Pedidos (INT-05) se consume solo como estadística agregada, no como lista de pedidos. Si falla, se usa el
 * snapshot del período exacto (DM-09).
 */
@Service
public class AnalyticsService {
    private  final ResenaRepository resenaRepository;
    private final MetricaRepository metricaRepository;
    private final EventoAnaliticoRepository eventoAnaliticoRepository;
    private final PedidosClient pedidosClient;


    public AnalyticsService(ResenaRepository resenaRepository, MetricaRepository metricaRepository, EventoAnaliticoRepository eventoAnaliticoRepository,PedidosClient pedidosClient) {
        this.resenaRepository = resenaRepository;
        this.metricaRepository = metricaRepository;
        this.eventoAnaliticoRepository = eventoAnaliticoRepository;
        this.pedidosClient = pedidosClient;
    }

    /**
     * Genera el tablero del período (CU-05). Si {@code desde} o {@code hasta} son null, toman la fecha actual del servidor.
     *
     * Pasos:
     * <ol>
     *   <li>Cuenta reseñas del período y guarda los snapshots de cantidad y promedio.</li>
     *   <li>Pide a Pedidos la cantidad de entregados (INT-05). Si falla, usa el snapshot del período exacto y marca {@code parcial = true}.</li>
     *   <li>Agrega cancelaciones (CU-08), pagos (CU-09) y entregas (CU-10).</li>
     * </ol>
     *
     * Decisión: al ser {@code @Transactional}, si el paso 2 no tiene snapshot previo, la excepción revierte todos los
     * snapshots escritos antes. El tablero es todo o nada.
     *
     * @param desde inicio del período (inclusive); null = hoy
     * @param hasta fin del período (inclusive); null = hoy
     * @throws InvalidPeriodException si {@code desde} es posterior a {@code hasta}
     * @throws ExternalServiceUnavailableException si Pedidos no responde y no hay snapshot del período exacto
     */
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
            // Validación de la respuesta de INT-05: cualquier inconsistencia se trata como indisponibilidad.
            OrdersStatsDTO stats = pedidosClient.obtenerEstadisticas("ENTREGADO", desde, hasta);
            if (stats == null || stats.getCantidad() == null || stats.getCantidad() < 0
                    || !"ENTREGADO".equals(stats.getEstado())
                    || !desde.equals(stats.getPeriodoDesde()) || !hasta.equals(stats.getPeriodoHasta())) {
                throw new ExternalServiceUnavailableException("Pedidos devolvió estadísticas inválidas");
            }
            metricas.add(guardarSnapshot(FuenteMetrica.PEDIDOS, TipoMetrica.PEDIDOS_ENTREGADOS,
                    desde, hasta, BigDecimal.valueOf(stats.getCantidad())));
        } catch (ExternalServiceUnavailableException e) {
            // Degradación (DM-09): se usa el snapshot del período exacto si existe. Si no existe, el error se propaga.
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
        
        // Llamadas internas (analizarPedidosCancelados, analizarPagos, analizarEntregas): no pasan por el proxy de
        // @Transactional, pero el método externo ya es transaccional y todas comparten esa transacción.
        //CU-08: pedidos cancelados en el tablero
        metricas.add(analizarPedidosCancelados(desde, hasta));
        
        // CU-09 / DM-18: las métricas de pagos forman parte del mismo tablero.
        metricas.addAll(analizarPagos(desde, hasta));

        // CU-10 / DM-18: entregas y tiempo promedio en el mismo tablero.
        metricas.addAll(analizarEntregas(desde, hasta));

        // 5- Devolver al controller
        return new DashboardResponse(LocalDateTime.now(), metricas, parcial);

    }

    /**
     * Consulta los snapshots cuyo período está completamente contenido en el rango pedido (CU-07, SRV-05).
     *
     * Solo lee la tabla {@code metricas}. No recalcula ni llama a otros módulos.
     *
     * @throws InvalidPeriodException si alguna fecha es null o el rango está invertido
     */
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

    /**
     * Cuenta pagos aprobados y rechazados del período y guarda ambos snapshots (SRV-07, CU-09).
     *
     * Usa {@code LocalTime.MAX} como fin del día para incluir todo el día {@code periodoHasta}.
     */
    // SRV-07 / REP-07 (CU-09): contar eventos de pagos y actualizar sus snapshots.
    @Transactional
    public List<MetricaResumen> analizarPagos(LocalDate periodoDesde, LocalDate periodoHasta) {
        LocalDate hoy = LocalDate.now();
        periodoDesde = periodoDesde == null ? hoy : periodoDesde;
        periodoHasta = periodoHasta == null ? hoy : periodoHasta;
        if (periodoDesde.isAfter(periodoHasta)) {
            throw new InvalidPeriodException("El periodo es invalido");
        }

        LocalDateTime desde = periodoDesde.atStartOfDay();
        LocalDateTime hasta = periodoHasta.atTime(LocalTime.MAX);
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
     * Cuenta entregas completadas y calcula el tiempo promedio de entrega (CU-10).
     *
     * Decisión: solo se promedian los eventos que traen {@code valor} (minutos). Si ninguno lo trae, el tiempo promedio
     * es {@code null} y no se persiste, porque {@code Metrica.valor} es NOT NULL.
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
        LocalDateTime hasta = periodoHasta.atTime(LocalTime.MAX);
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
         * Registra un evento analítico recibido de otro módulo (SRV-04, CU-06).
         *
         * Valida RN-09 antes de guardar. Los eventos se guardan crudos; su análisis ocurre después, en los métodos de
         * CU-08 a CU-10.
         *
         * @throws InvalidAnalyticsEventException si falta alguno de los campos obligatorios de RN-09
         */
        // SRV-04 (CU-06)
    @Transactional
    public EventoAnaliticoResponse registrarEvento(EventoAnaliticoRequest request) {

        // RN-09: campos obligatorios
        validarEvento(request);

        // REP-06
        EventoAnalitico evento = EventoAnaliticoMapper.toEntity(request);
        eventoAnaliticoRepository.save(evento);

        return EventoAnaliticoMapper.toResponse(evento);
    }

    // Observación: no se valida que entidadId sea positivo ni que fechaEvento no sea futura. Queda como mejora pendiente.
    // RN-09: tipoEvento, moduloOrigen, entidadId y fechaEvento son obligatorios
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
    }


/**
 * Guarda un snapshot con upsert (RN-06).
 *
 * 1. REP-05 busca el snapshot del mismo {@code fuente}, {@code tipo} y período.
 * 2. Si existe, actualiza {@code valor} y {@code fechaCalculo} sobre la entidad administrada: Hibernate lo detecta
 *    por dirty checking y emite un UPDATE. Si no existe, crea una entidad nueva.
 * 3. {@code save} persiste la entidad nueva. Sobre una entidad ya administrada es redundante, pero no hace daño.
 *
 * Observación: dos ejecuciones simultáneas para el mismo período pueden intentar insertar dos filas. La restricción
 * {@code uk_metrica_fuente_tipo_periodo} rechaza la segunda. No hay manejo específico de ese error.
 */
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

/**
 * Cuenta cancelaciones de pedidos del período y guarda el snapshot (SRV-06, CU-08).
 *
 * Observación: el criterio de cancelación depende del texto {@code "CANCELADO"} en {@code detalle}, enviado por
 * Pedidos. Un enum tipado en el evento sería más robusto. Es una alternativa a evaluar, no un defecto confirmado.
 */
    // SRV-06 (CU-08): cuenta eventos de cancelación del período y guarda el snapshot (RN-03, RN-06)
    @Transactional
    public MetricaResumen analizarPedidosCancelados(LocalDate periodoDesde, LocalDate periodoHasta) {

        // REP-07 filtra por tipo y fecha. Between es inclusivo: el fin es el último instante
        // del día con 6 decimales, para que el día "hasta" entre completo.
        List<EventoAnalitico> eventos = eventoAnaliticoRepository.findByTipoEventoAndFechaEventoBetween(
                TipoEvento.PEDIDO_ESTADO_CAMBIADO,
                periodoDesde.atStartOfDay(),
                periodoHasta.atTime(23, 59, 59, 999_999_000));

        // REP-07 no filtra por detalle: de todos los cambios de estado, solo cuentan los CANCELADO
        long cancelados = eventos.stream()
                .filter(e -> "CANCELADO".equals(e.getDetalle()))
                .count();

        // Sin cancelaciones el valor es 0 y se persiste (a diferencia del promedio, que es null)
        return guardarSnapshot(FuenteMetrica.PEDIDOS, TipoMetrica.CANTIDAD_PEDIDOS_CANCELADOS,
                periodoDesde, periodoHasta, BigDecimal.valueOf(cancelados));
    }

}
