package ar.edu.uade.api.deliverygo.services;

import ar.edu.uade.api.deliverygo.dto.DashboardResponse;
import ar.edu.uade.api.deliverygo.dto.EventoAnaliticoRequest;
import ar.edu.uade.api.deliverygo.dto.MetricaResumen;
import ar.edu.uade.api.deliverygo.excepcion.InvalidAnalyticsEventException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidPeriodException;
import ar.edu.uade.api.deliverygo.model.EventoAnalitico;
import ar.edu.uade.api.deliverygo.model.FuenteMetrica;
import ar.edu.uade.api.deliverygo.model.Metrica;
import ar.edu.uade.api.deliverygo.model.ModuloOrigen;
import ar.edu.uade.api.deliverygo.model.OrigenMetrica;
import ar.edu.uade.api.deliverygo.model.Resena;
import ar.edu.uade.api.deliverygo.model.TipoEvento;
import ar.edu.uade.api.deliverygo.model.TipoMetrica;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.repository.EventoAnaliticoRepository;
import ar.edu.uade.api.deliverygo.repository.MetricaRepository;
import ar.edu.uade.api.deliverygo.repository.ResenaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SRV-03 a SRV-07 (CU-05 a CU-10): tablero, períodos e indicadores calculados desde reseñas y eventos (DM-21).
 * Los repositorios son mocks. Un repositorio sin configurar devuelve lista vacía u Optional vacío.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalyticsServiceTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 11, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 3);

    @Mock
    private ResenaRepository resenaRepository;
    @Mock
    private MetricaRepository metricaRepository;
    @Mock
    private EventoAnaliticoRepository eventoAnaliticoRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    // ---------- Datos de prueba ----------

    private MetricaResumen metrica(DashboardResponse tablero, TipoMetrica tipo) {
        return tablero.getMetricas().stream().filter(m -> m.getTipoMetrica() == tipo).findFirst().orElseThrow();
    }

    private Resena resena(int puntuacion) {
        return new Resena(100L, 1L, TipoObjetivo.COMERCIO, 10L, puntuacion, null);
    }

    private EventoAnalitico evento(TipoEvento tipo, ModuloOrigen modulo, BigDecimal valor, String detalle) {
        return new EventoAnalitico(tipo, modulo, 5L, valor, detalle, LocalDateTime.of(2026, 11, 2, 10, 0));
    }

    // ---------- CU-05: períodos de fechas ----------

    @Test
    void tablero_conDesdePosteriorAHasta_lanzaInvalidPeriod() {
        assertThrows(InvalidPeriodException.class, () -> analyticsService.generarTablero(HASTA, DESDE));
    }

    // RN-03: ambos extremos son inclusivos, así que desde = hasta es un período válido de un día
    @Test
    void tablero_conUnSoloDia_esValido() {
        DashboardResponse tablero = analyticsService.generarTablero(DESDE, DESDE);

        assertEquals(8, tablero.getMetricas().size());
        assertEquals(DESDE, metrica(tablero, TipoMetrica.CANTIDAD_RESENAS).getPeriodoHasta());
    }

    // CU-05: si se omiten las fechas, el período es el día de hoy
    @Test
    void tablero_sinFechas_usaLaFechaActual() {
        DashboardResponse tablero = analyticsService.generarTablero(null, null);

        LocalDate hoy = LocalDate.now();
        assertEquals(hoy, metrica(tablero, TipoMetrica.CANTIDAD_RESENAS).getPeriodoDesde());
        assertEquals(hoy, metrica(tablero, TipoMetrica.CANTIDAD_RESENAS).getPeriodoHasta());
    }

    // REP-09: las reseñas se buscan desde el inicio de "desde" hasta el inicio del día siguiente a "hasta" (exclusivo)
    @Test
    void tablero_buscaLasResenasDelPeriodoCompleto() {
        analyticsService.generarTablero(DESDE, HASTA);

        ArgumentCaptor<LocalDateTime> desdeBuscado = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hastaBuscado = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(resenaRepository).findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(
                desdeBuscado.capture(), hastaBuscado.capture());
        assertEquals(LocalDateTime.of(2026, 11, 1, 0, 0), desdeBuscado.getValue());
        assertEquals(LocalDateTime.of(2026, 11, 4, 0, 0), hastaBuscado.getValue());
    }

    // REP-07: los eventos se buscan desde el inicio de "desde" hasta el final del día "hasta"
    @Test
    void tablero_buscaLosEventosDePagosDelPeriodoCompleto() {
        analyticsService.generarTablero(DESDE, HASTA);

        ArgumentCaptor<LocalDateTime> desdeBuscado = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hastaBuscado = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(eventoAnaliticoRepository).findByTipoEventoAndFechaEventoBetween(
                eq(TipoEvento.PAGO_APROBADO), desdeBuscado.capture(), hastaBuscado.capture());
        assertEquals(DESDE.atStartOfDay(), desdeBuscado.getValue());
        assertEquals(LocalDateTime.of(2026, 11, 3, 23, 59, 59, 999_999_000), hastaBuscado.getValue());
    }

    @Test
    void analizarPagos_conPeriodoInvertido_lanzaInvalidPeriod() {
        assertThrows(InvalidPeriodException.class, () -> analyticsService.analizarPagos(HASTA, DESDE));
    }

    @Test
    void analizarEntregas_conPeriodoInvertido_lanzaInvalidPeriod() {
        assertThrows(InvalidPeriodException.class, () -> analyticsService.analizarEntregas(HASTA, DESDE));
    }

    // ---------- CU-07: consulta de snapshots por período ----------

    @Test
    void consultarMetricas_sinFechas_lanzaInvalidPeriod() {
        assertThrows(InvalidPeriodException.class, () -> analyticsService.consultarMetricasPorPeriodo(null, HASTA));
        assertThrows(InvalidPeriodException.class, () -> analyticsService.consultarMetricasPorPeriodo(DESDE, null));
    }

    @Test
    void consultarMetricas_conPeriodoInvertido_lanzaInvalidPeriod() {
        assertThrows(InvalidPeriodException.class, () -> analyticsService.consultarMetricasPorPeriodo(HASTA, DESDE));
    }

    @Test
    void consultarMetricas_devuelveLosSnapshotsGuardados() {
        Metrica guardada = new Metrica(FuenteMetrica.RESENAS, TipoMetrica.CANTIDAD_RESENAS, DESDE, HASTA,
                new BigDecimal("3"), LocalDateTime.now());
        when(metricaRepository.findByPeriodoDesdeGreaterThanEqualAndPeriodoHastaLessThanEqual(DESDE, HASTA))
                .thenReturn(List.of(guardada));

        List<MetricaResumen> metricas = analyticsService.consultarMetricasPorPeriodo(DESDE, HASTA);

        assertEquals(1, metricas.size());
        assertEquals(TipoMetrica.CANTIDAD_RESENAS, metricas.get(0).getTipoMetrica());
    }

    // CU-07: un período sin snapshots devuelve una lista vacía, no un error
    @Test
    void consultarMetricas_sinSnapshots_devuelveListaVacia() {
        assertTrue(analyticsService.consultarMetricasPorPeriodo(DESDE, HASTA).isEmpty());
    }

    // ---------- Origen y cálculo de cada indicador ----------

    // Sin datos: los conteos valen 0 y se guardan; los promedios son null y no se guardan (Metrica.valor es NOT NULL)
    @Test
    void tablero_sinDatos_conteosEnCeroYPromediosNull() {
        DashboardResponse tablero = analyticsService.generarTablero(DESDE, HASTA);

        assertEquals(0, metrica(tablero, TipoMetrica.CANTIDAD_RESENAS).getValor().compareTo(BigDecimal.ZERO));
        assertNull(metrica(tablero, TipoMetrica.PROMEDIO_CALIFICACION).getValor());
        assertNull(metrica(tablero, TipoMetrica.TIEMPO_PROMEDIO_ENTREGA).getValor());
        // Se guardan 6 de los 8: no se guardan los dos promedios
        verify(metricaRepository, times(6)).save(any(Metrica.class));
    }

    // CU-05: reseñas de 5 y 4 dan cantidad 2 y promedio 4.50
    @Test
    void tablero_conResenas_calculaCantidadYPromedio() {
        when(resenaRepository.findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(any(), any()))
                .thenReturn(List.of(resena(5), resena(4)));

        DashboardResponse tablero = analyticsService.generarTablero(DESDE, HASTA);

        assertEquals(0, metrica(tablero, TipoMetrica.CANTIDAD_RESENAS).getValor().compareTo(new BigDecimal("2")));
        assertEquals(new BigDecimal("4.50"), metrica(tablero, TipoMetrica.PROMEDIO_CALIFICACION).getValor());
    }

    // CU-08, CU-09, CU-10: los indicadores salen de los eventos que enviaron otros módulos
    @Test
    void tablero_cuentaLosEventosDeOtrosModulos() {
        when(eventoAnaliticoRepository.findByTipoEventoAndFechaEventoBetween(eq(TipoEvento.PEDIDO_ESTADO_CAMBIADO), any(), any()))
                .thenReturn(List.of(
                        evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, null, "CANCELADO"),
                        evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, null, "CANCELADO"),
                        evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, null, "ENTREGADO")));
        when(eventoAnaliticoRepository.findByTipoEventoAndFechaEventoBetween(eq(TipoEvento.PAGO_APROBADO), any(), any()))
                .thenReturn(List.of(
                        evento(TipoEvento.PAGO_APROBADO, ModuloOrigen.PAGOS, null, null),
                        evento(TipoEvento.PAGO_APROBADO, ModuloOrigen.PAGOS, null, null),
                        evento(TipoEvento.PAGO_APROBADO, ModuloOrigen.PAGOS, null, null)));
        when(eventoAnaliticoRepository.findByTipoEventoAndFechaEventoBetween(eq(TipoEvento.PAGO_RECHAZADO), any(), any()))
                .thenReturn(List.of(evento(TipoEvento.PAGO_RECHAZADO, ModuloOrigen.PAGOS, null, null)));
        when(eventoAnaliticoRepository.findByTipoEventoAndFechaEventoBetween(eq(TipoEvento.ENTREGA_COMPLETADA), any(), any()))
                .thenReturn(List.of(
                        evento(TipoEvento.ENTREGA_COMPLETADA, ModuloOrigen.LOGISTICA, new BigDecimal("30"), null),
                        evento(TipoEvento.ENTREGA_COMPLETADA, ModuloOrigen.LOGISTICA, new BigDecimal("40"), null),
                        evento(TipoEvento.ENTREGA_COMPLETADA, ModuloOrigen.LOGISTICA, null, null)));

        DashboardResponse tablero = analyticsService.generarTablero(DESDE, HASTA);

        // Solo cuentan los cambios de estado con detalle CANCELADO
        assertEquals(0, metrica(tablero, TipoMetrica.CANTIDAD_PEDIDOS_CANCELADOS).getValor().compareTo(new BigDecimal("2")));
        assertEquals(0, metrica(tablero, TipoMetrica.CANTIDAD_PAGOS_APROBADOS).getValor().compareTo(new BigDecimal("3")));
        assertEquals(0, metrica(tablero, TipoMetrica.CANTIDAD_PAGOS_RECHAZADOS).getValor().compareTo(BigDecimal.ONE));
        // Las 3 entregas cuentan, pero el tiempo promedio solo usa las 2 que traen valor: (30 + 40) / 2
        assertEquals(0, metrica(tablero, TipoMetrica.CANTIDAD_ENTREGAS).getValor().compareTo(new BigDecimal("3")));
        assertEquals(new BigDecimal("35.00"), metrica(tablero, TipoMetrica.TIEMPO_PROMEDIO_ENTREGA).getValor());
    }

    // DM-21: el tablero no consulta a otros módulos; los pedidos entregados salen de los eventos recibidos
    @Test
    void tablero_cuentaLosPedidosEntregadosDesdeLosEventos() {
        when(eventoAnaliticoRepository.findByTipoEventoAndFechaEventoBetween(eq(TipoEvento.PEDIDO_ESTADO_CAMBIADO), any(), any()))
                .thenReturn(List.of(
                        evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, null, "ENTREGADO"),
                        evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, null, "ENTREGADO"),
                        evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, null, "CANCELADO"),
                        evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, null, "EN_CAMINO")));

        DashboardResponse tablero = analyticsService.generarTablero(DESDE, HASTA);

        assertEquals(0, metrica(tablero, TipoMetrica.PEDIDOS_ENTREGADOS).getValor().compareTo(new BigDecimal("2")));
        assertEquals(0, metrica(tablero, TipoMetrica.CANTIDAD_PEDIDOS_CANCELADOS).getValor().compareTo(BigDecimal.ONE));
    }

    // CU-08: sin eventos, los pedidos entregados valen 0 y se guardan
    @Test
    void analizarPedidosEntregados_sinEventos_valeCeroYSeGuarda() {
        MetricaResumen entregados = analyticsService.analizarPedidosEntregados(DESDE, HASTA);

        assertEquals(0, entregados.getValor().compareTo(BigDecimal.ZERO));
        verify(metricaRepository).save(any(Metrica.class));
    }

    // CU-05: cada indicador informa de dónde sale el dato y cómo se calcula
    @Test
    void tablero_cadaIndicadorInformaOrigenYFormula() {
        DashboardResponse tablero = analyticsService.generarTablero(DESDE, HASTA);

        for (MetricaResumen metrica : tablero.getMetricas()) {
            assertNotNull(metrica.getOrigen());
            assertFalse(metrica.getFormula().isBlank());
        }
        assertEquals(OrigenMetrica.RESENAS_PROPIAS, metrica(tablero, TipoMetrica.CANTIDAD_RESENAS).getOrigen());
        assertEquals(OrigenMetrica.RESENAS_PROPIAS, metrica(tablero, TipoMetrica.PROMEDIO_CALIFICACION).getOrigen());
        assertEquals(OrigenMetrica.EVENTOS_RECIBIDOS, metrica(tablero, TipoMetrica.PEDIDOS_ENTREGADOS).getOrigen());
        assertEquals(OrigenMetrica.EVENTOS_RECIBIDOS, metrica(tablero, TipoMetrica.TIEMPO_PROMEDIO_ENTREGA).getOrigen());
    }

    // ---------- CU-06: registro de eventos (RN-09) ----------

    private EventoAnaliticoRequest eventoRequest(Long entidadId, LocalDateTime fecha) {
        return new EventoAnaliticoRequest(TipoEvento.PAGO_APROBADO, ModuloOrigen.PAGOS, entidadId, null, null, fecha);
    }

    @Test
    void registrarEvento_valido_guardaElEvento() {
        analyticsService.registrarEvento(eventoRequest(5L, LocalDateTime.now().minusHours(1)));

        verify(eventoAnaliticoRepository).save(any(EventoAnalitico.class));
    }

    @Test
    void registrarEvento_sinCamposObligatorios_lanzaInvalidAnalyticsEvent() {
        EventoAnaliticoRequest vacio = new EventoAnaliticoRequest(null, null, null, null, null, null);

        assertThrows(InvalidAnalyticsEventException.class, () -> analyticsService.registrarEvento(vacio));

        verify(eventoAnaliticoRepository, never()).save(any(EventoAnalitico.class));
    }

    @Test
    void registrarEvento_conEntidadIdNoPositivo_lanzaInvalidAnalyticsEvent() {
        assertThrows(InvalidAnalyticsEventException.class,
                () -> analyticsService.registrarEvento(eventoRequest(0L, LocalDateTime.now().minusHours(1))));
        assertThrows(InvalidAnalyticsEventException.class,
                () -> analyticsService.registrarEvento(eventoRequest(-3L, LocalDateTime.now().minusHours(1))));
    }

    @Test
    void registrarEvento_conFechaFutura_lanzaInvalidAnalyticsEvent() {
        assertThrows(InvalidAnalyticsEventException.class,
                () -> analyticsService.registrarEvento(eventoRequest(5L, LocalDateTime.now().plusDays(1))));
    }

    // RN-09: se tolera 1 minuto de diferencia de reloj entre módulos
    @Test
    void registrarEvento_conFechaUnPocoAdelantada_seAcepta() {
        analyticsService.registrarEvento(eventoRequest(5L, LocalDateTime.now().plusSeconds(30)));

        verify(eventoAnaliticoRepository).save(any(EventoAnalitico.class));
    }
}
