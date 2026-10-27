package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.PedidosClient;
import ar.edu.uade.api.deliverygo.dto.OrdersStatsDTO;
import ar.edu.uade.api.deliverygo.dto.PedidoDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

//Mock de Pedidos (DM-11)
//@Component para que Spring los inyecte como implementación de las interfaces
@Component
public class PedidosClientMock implements PedidosClient {

    private final Map<Long, PedidoDTO> pedidos = Map.of(1L, new PedidoDTO(1L, 100L, 10L, null, "ENTREGADO"), 2L, new PedidoDTO(2L, 100L, null, 20L, "ENTREGADO"), 3L, new PedidoDTO(3L, 100L, 10L, null, "EN_CAMINO")
    );

    @Override
    public Optional<PedidoDTO> obtenerPedido(Long pedidoId) {
        return Optional.ofNullable(pedidos.get(pedidoId));
    }

    @Override

    public OrdersStatsDTO obtenerEstadisticas(String estado, LocalDate periodoDesde, LocalDate periodoHasta) {
        //Filtrar los pedidosDTO por estados iguales y contarlos
        long cantidad = pedidos.values().stream().filter(p -> p.getEstado().equals(estado)).count();
        return new OrdersStatsDTO(estado, periodoDesde, periodoHasta, cantidad);
    }
}
