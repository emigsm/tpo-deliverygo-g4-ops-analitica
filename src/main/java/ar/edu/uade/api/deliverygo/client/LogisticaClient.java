package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.RepartidorDTO;

import java.util.Optional;

//Adapter a módulo Logística (DM-11)
public interface LogisticaClient {

    // GET /api/v1/couriers/{id} (INT-04) devuelve 200, 404, 503.
    Optional<RepartidorDTO> obtenerRepartidor(Long repartidorId);
}
