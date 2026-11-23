package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.ComercioClient;
import ar.edu.uade.api.deliverygo.dto.ComercioDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Implementación de prueba de {@link ar.edu.uade.api.deliverygo.client.ComercioClient}, activa fuera del perfil {@code integracion}.
 *
 * Datos fijos: el comercio {@code 10} existe y está activo. Cualquier otro id devuelve vacío.
 */
//Mock de Comercio (DM-11)
//@Component para que Spring los inyecte como implementación de las interfaces
@Component
@Profile("!integracion")
public class ComercioClientMock implements ComercioClient {

    private final Map<Long, ComercioDTO> comercios = Map.of(10L, new ComercioDTO(10L, true));

    @Override
    public Optional<ComercioDTO> obtenerComercio(Long comercioId) {
        return Optional.ofNullable(comercios.get(comercioId));
    }
}
