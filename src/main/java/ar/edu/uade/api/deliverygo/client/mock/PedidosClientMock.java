package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.PedidosClient;
import ar.edu.uade.api.deliverygo.dto.OrdersStatsDTO;
import ar.edu.uade.api.deliverygo.dto.PedidoDTO;
import ar.edu.uade.api.deliverygo.excepcion.ExternalServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

//Mock de Pedidos (DM-11)
//@Component para que Spring los inyecte como implementación de las interfaces
@Component
public class PedidosClientMock implements PedidosClient {

    // Configuración exclusiva de CU-05; no altera obtenerPedido (CU-01/CU-02).
    @Value("${app.pedidos.mock.stats.disponible:true}")
    private boolean estadisticasDisponibles;

    @Value("${app.pedidos.mock.stats.cantidad-entregados:2}")
    private long cantidadEntregados;

    private final Map<Long, PedidoDTO> pedidos = Map.of(1L, new PedidoDTO(1L, 100L, 10L, null, "ENTREGADO"), 2L, new PedidoDTO(2L, 100L, null, 20L, "ENTREGADO"), 3L, new PedidoDTO(3L, 100L, 10L, null, "EN_CAMINO")
    );

    @Override
    public Optional<PedidoDTO> obtenerPedido(Long pedidoId) {
        return Optional.ofNullable(pedidos.get(pedidoId));
    }

    @Override
    public OrdersStatsDTO obtenerEstadisticas(String estado, LocalDate periodoDesde, LocalDate periodoHasta) {
        if (!estadisticasDisponibles || cantidadEntregados < 0) {
            throw new ExternalServiceUnavailableException("Estadísticas de Pedidos no disponibles (mock)");
        }
        // Agregado simulado para el período solicitado; no consulta pedidos individuales.
        return new OrdersStatsDTO(estado, periodoDesde, periodoHasta, cantidadEntregados);
    }
}
