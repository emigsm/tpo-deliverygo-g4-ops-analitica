package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.ComercioClient;
import ar.edu.uade.api.deliverygo.dto.ComercioDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

//Mock de Comercio (DM-11)
//@Component para que Spring los inyecte como implementación de las interfaces
@Component
public class ComercioClientMock implements ComercioClient {

    private final Map<Long, ComercioDTO> comercios = Map.of(10L, new ComercioDTO(10L, true));

    @Override
    public Optional<ComercioDTO> obtenerComercio(Long comercioId) {
        return Optional.ofNullable(comercios.get(comercioId));
    }
}
