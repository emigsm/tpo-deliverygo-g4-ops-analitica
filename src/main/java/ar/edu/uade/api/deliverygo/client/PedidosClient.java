package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.PedidoDTO;

import java.util.Optional;

/**
 * DM-11: adapter hacia el módulo Pedidos. Hoy solo existe PedidosClientMock, sin cliente HTTP.
 */
public interface PedidosClient {

    // INT-01 (RN-01, RN-07): devuelve el pedido, o vacío si no existe (404).
    // Si el módulo no responde lanza ExternalServiceUnavailableException (503)
    Optional<PedidoDTO> obtenerPedido(Long pedidoId);
}
