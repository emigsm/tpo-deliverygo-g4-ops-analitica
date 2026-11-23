package ar.edu.uade.api.deliverygo.client;
import ar.edu.uade.api.deliverygo.dto.ComercioDTO;
import java.util.Optional;

/**
 * Puerto (adapter, DM-11) hacia el módulo Comercios / Catálogo (INT-03).
 *
 * Dos implementaciones, seleccionadas por perfil: {@code ComercioHttpClient} (perfil {@code integracion}) y
 * {@code ComercioClientMock} (resto). {@code ReviewService} depende solo de esta interfaz.
 *
 * Contrato: {@code Optional.empty()} para un comercio inexistente (404); {@code ExternalServiceUnavailableException}
 * para indisponibilidad.
 *
 * Observación: el comentario del método dice {@code /api/v1/merchants/{id}} y el cliente HTTP llama a
 * {@code /api/v1/merchant/{id}}. Ver guía de observaciones.
 */
//Adapter al módulo Comercios (DM-11)
public interface ComercioClient {

    // GET /api/v1/merchant/{id} (INT-03) devuelve 200, 404, 503
    Optional<ComercioDTO> obtenerComercio(Long comercioId);
}
