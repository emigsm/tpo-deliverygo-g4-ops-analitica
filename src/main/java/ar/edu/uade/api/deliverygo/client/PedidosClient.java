package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.OrdersStatsDTO;
import ar.edu.uade.api.deliverygo.dto.PedidoDTO;

import java.time.LocalDate;
import java.util.Optional;

// Adapter a el módulo Pedidos (DM-11)
public interface PedidosClient {
    //GET /api/v1/orders/{id} (INT-01) Devuelve 200, 404 (Optional.empty()), 503
    Optional<PedidoDTO> obtenerPedido(Long pedidoId);

    //GET /api/v1/orders/stats (DM-08, validar) (INT-05). Devuelve 200, 503
    OrdersStatsDTO obtenerEstadisticas(String estado, LocalDate periodoDesde, LocalDate periodoHasta);
}
