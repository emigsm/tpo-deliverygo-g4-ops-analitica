package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.RepartidorDTO;

import java.util.Optional;

/**
 * Puerto (adapter, DM-11) hacia el módulo Logística (INT-04). Devuelve el repartidor o vacío si no existe.
 *
 * Observación: solo existe la implementación mock ({@code LogisticaClientMock}), sin perfil. No hay cliente HTTP en el repositorio.
 */
//Adapter a módulo Logística (DM-11)
public interface LogisticaClient {

    // GET /api/v1/couriers/{id} (INT-04) devuelve 200, 404, 503.
    Optional<RepartidorDTO> obtenerRepartidor(Long repartidorId);
}
