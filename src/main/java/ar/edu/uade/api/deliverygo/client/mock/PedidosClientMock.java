package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.PedidosClient;
import ar.edu.uade.api.deliverygo.dto.PedidoDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * DM-11: mock de Pedidos (INT-01). Un pedido por cada situación que se quiere mostrar en la demo:
 * 1 y 2 entregados del usuario 100 (comercio 10 y repartidor 20), 3 en camino (RN-01), 4 entregado del usuario 200 (RN-07),
 * 5 con comercio inactivo, 6 de un usuario inactivo y 7 con repartidor inactivo (RN-08), 8 cancelado (RN-01)
 * y 9 entregado con comercio y repartidor, para reseñar ambos del mismo pedido (DM-02).
 */
@Component
public class PedidosClientMock implements PedidosClient {

    private final Map<Long, PedidoDTO> pedidos = Map.of(
            1L, new PedidoDTO(1L, 100L, 10L, null, "ENTREGADO"),
            2L, new PedidoDTO(2L, 100L, null, 20L, "ENTREGADO"),
            3L, new PedidoDTO(3L, 100L, 10L, null, "EN_CAMINO"),
            4L, new PedidoDTO(4L, 200L, 10L, null, "ENTREGADO"),
            5L, new PedidoDTO(5L, 100L, 11L, null, "ENTREGADO"),
            6L, new PedidoDTO(6L, 300L, 10L, null, "ENTREGADO"),
            7L, new PedidoDTO(7L, 100L, null, 21L, "ENTREGADO"),
            8L, new PedidoDTO(8L, 100L, 10L, null, "CANCELADO"),
            9L, new PedidoDTO(9L, 100L, 10L, 20L, "ENTREGADO"));

    @Override
    public Optional<PedidoDTO> obtenerPedido(Long pedidoId) {
        return Optional.ofNullable(pedidos.get(pedidoId));
    }
}
