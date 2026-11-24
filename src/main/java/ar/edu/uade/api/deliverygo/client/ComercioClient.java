package ar.edu.uade.api.deliverygo.client;
import ar.edu.uade.api.deliverygo.dto.ComercioDTO;
import java.util.Optional;

/**
 * DM-11: adapter hacia el módulo Comercios (INT-03). ReviewService depende solo de esta interfaz.
 * Perfil integracion: ComercioHttpClient. Resto: ComercioClientMock.
 */
public interface ComercioClient {

    // INT-03 (RN-08): devuelve el comercio, o vacío si no existe (404).
    // Si el módulo no responde lanza ExternalServiceUnavailableException (503)
    Optional<ComercioDTO> obtenerComercio(Long comercioId);
}
