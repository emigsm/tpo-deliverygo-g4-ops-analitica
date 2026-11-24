package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.RepartidorDTO;

import java.util.Optional;

/**
 * DM-11: adapter hacia el módulo Logística (INT-04). Hoy solo existe LogisticaClientMock, sin cliente HTTP.
 */
public interface LogisticaClient {

    // INT-04 (RN-08): devuelve el repartidor, o vacío si no existe (404).
    // Si el módulo no responde lanza ExternalServiceUnavailableException (503)
    Optional<RepartidorDTO> obtenerRepartidor(Long repartidorId);
}
