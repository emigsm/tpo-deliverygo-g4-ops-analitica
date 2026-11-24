package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.ComercioClient;
import ar.edu.uade.api.deliverygo.dto.ComercioDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * DM-11: mock de Comercios (INT-03), activo fuera del perfil integracion.
 * Datos fijos: el comercio 10 está activo y el 11 existe pero está inactivo (RN-08).
 */
@Component
@Profile("!integracion")
public class ComercioClientMock implements ComercioClient {

    private final Map<Long, ComercioDTO> comercios = Map.of(
            10L, new ComercioDTO(10L, true),
            11L, new ComercioDTO(11L, false));

    @Override
    public Optional<ComercioDTO> obtenerComercio(Long comercioId) {
        return Optional.ofNullable(comercios.get(comercioId));
    }
}
