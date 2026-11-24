package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.LogisticaClient;
import ar.edu.uade.api.deliverygo.dto.RepartidorDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * DM-11: mock de Logística (INT-04).
 * Datos fijos: el repartidor 20 está activo y el 21 existe pero está inactivo (RN-08).
 */
@Component
public class LogisticaClientMock implements LogisticaClient {

    private final Map<Long, RepartidorDTO> repartidores = Map.of(
            20L, new RepartidorDTO(20L, true),
            21L, new RepartidorDTO(21L, false));

    @Override
    public Optional<RepartidorDTO> obtenerRepartidor(Long repartidorId) {
        return Optional.ofNullable(repartidores.get(repartidorId));
    }
}
