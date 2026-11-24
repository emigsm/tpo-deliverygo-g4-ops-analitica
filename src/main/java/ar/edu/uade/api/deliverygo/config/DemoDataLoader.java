package ar.edu.uade.api.deliverygo.config;

import ar.edu.uade.api.deliverygo.model.EventoAnalitico;
import ar.edu.uade.api.deliverygo.model.ModuloOrigen;
import ar.edu.uade.api.deliverygo.model.Resena;
import ar.edu.uade.api.deliverygo.model.TipoEvento;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.repository.EventoAnaliticoRepository;
import ar.edu.uade.api.deliverygo.repository.ResenaRepository;
import ar.edu.uade.api.deliverygo.services.AnalyticsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Datos de demostración para poder probar cada CU al arrancar, solo fuera del perfil integracion.
 * CU-03 y CU-04: dos reseñas del usuario 101 (comercio 10 y repartidor 20). Los pedidos 901 y 902 no existen en
 * PedidosClientMock, así que no chocan con RN-02 al crear reseñas de los pedidos mock.
 * CU-05 a CU-10: eventos de hoy que simulan lo que enviarían Pedidos, Pagos y Logística.
 * CU-07: se genera el tablero de hoy para que haya snapshots guardados que consultar.
 * Se guardan con los repositorios, sin pasar por los servicios: es un atajo solo para demo.
 */
@Configuration
@Profile("!integracion")
public class DemoDataLoader {

    @Bean
    public CommandLineRunner cargarDatosDemo(ResenaRepository resenaRepository,
                                             EventoAnaliticoRepository eventoRepository,
                                             AnalyticsService analyticsService) {
        return args -> {
            resenaRepository.save(new Resena(101L, 901L, TipoObjetivo.COMERCIO, 10L, 4, "Reseña demo precargada"));
            resenaRepository.save(new Resena(101L, 902L, TipoObjetivo.REPARTIDOR, 20L, 5, "Reseña demo precargada"));

            LocalDateTime hoy = LocalDate.now().atTime(9, 0);

            // CU-08: 3 pedidos entregados, 1 cancelado y 1 que solo cambió de estado
            for (long pedido = 1; pedido <= 3; pedido++) {
                eventoRepository.save(evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, pedido, null, "ENTREGADO", hoy));
            }
            eventoRepository.save(evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, 8L, null, "CANCELADO", hoy));
            eventoRepository.save(evento(TipoEvento.PEDIDO_ESTADO_CAMBIADO, ModuloOrigen.PEDIDOS, 3L, null, "EN_CAMINO", hoy));

            // CU-09: 4 pagos aprobados y 1 rechazado
            for (long pago = 1; pago <= 4; pago++) {
                eventoRepository.save(evento(TipoEvento.PAGO_APROBADO, ModuloOrigen.PAGOS, pago, null, null, hoy));
            }
            eventoRepository.save(evento(TipoEvento.PAGO_RECHAZADO, ModuloOrigen.PAGOS, 5L, null, null, hoy));

            // CU-10: 3 entregas de 28, 35 y 42 minutos (promedio 35.00)
            eventoRepository.save(evento(TipoEvento.ENTREGA_COMPLETADA, ModuloOrigen.LOGISTICA, 1L, new BigDecimal("28"), null, hoy));
            eventoRepository.save(evento(TipoEvento.ENTREGA_COMPLETADA, ModuloOrigen.LOGISTICA, 2L, new BigDecimal("35"), null, hoy));
            eventoRepository.save(evento(TipoEvento.ENTREGA_COMPLETADA, ModuloOrigen.LOGISTICA, 3L, new BigDecimal("42"), null, hoy));

            // CU-07: genera los snapshots del día para que GET /analytics/metrics devuelva datos
            analyticsService.generarTablero(null, null);
        };
    }

    private EventoAnalitico evento(TipoEvento tipo, ModuloOrigen modulo, Long entidadId, BigDecimal valor,
                                   String detalle, LocalDateTime fecha) {
        return new EventoAnalitico(tipo, modulo, entidadId, valor, detalle, fecha);
    }
}
