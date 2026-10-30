package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.OrdersStatsDTO;
import ar.edu.uade.api.deliverygo.dto.PedidoDTO;

import java.time.LocalDate;
import java.util.Optional;

// Adapter a el módulo Pedidos (DM-11)
public interface PedidosClient {
    //GET /api/v1/orders/{id} (INT-01) Devuelve 200, 404 (Optional.empty()), 503
    Optional<PedidoDTO> obtenerPedido(Long pedidoId);

    // INT-05 PROPUESTO, pendiente de confirmación con el equipo de Pedidos:
    // GET /api/v1/orders/stats?estado=ENTREGADO&periodoDesde=yyyy-MM-dd&periodoHasta=yyyy-MM-dd
    // 200: OrdersStatsDTO con estado/período coincidentes y cantidad no negativa.
    // Indisponibilidad: ExternalServiceUnavailableException (503). Sin consulta de pedidos individuales.
    OrdersStatsDTO obtenerEstadisticas(String estado, LocalDate periodoDesde, LocalDate periodoHasta);
}
