package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.LogisticaClient;
import ar.edu.uade.api.deliverygo.dto.RepartidorDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Implementación de prueba de {@link ar.edu.uade.api.deliverygo.client.LogisticaClient}.
 *
 * Datos fijos: el repartidor {@code 20} existe y está activo.
 *
 * Observación: no tiene {@code @Profile}, así que también corre con el perfil {@code integracion}.
 */
//Mock de Logística (DM-11)
//@Component para que Spring los inyecte como implementación de las interfaces
@Component
public class LogisticaClientMock implements LogisticaClient {

    private final Map<Long, RepartidorDTO> repartidores = Map.of(20L, new RepartidorDTO(20L, true));

    @Override
    public Optional<RepartidorDTO> obtenerRepartidor(Long repartidorId) {
        return Optional.ofNullable(repartidores.get(repartidorId));
    }
}
