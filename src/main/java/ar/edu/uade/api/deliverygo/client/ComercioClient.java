package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.ComercioDTO;

import java.util.Optional;

//Adapter al módulo Comercios (DM-11)
public interface ComercioClient {

    // GET /api/v1/merchants/{id} (INT-03) devuelve 200, 404, 503
    Optional<ComercioDTO> obtenerComercio(Long comercioId);
}
