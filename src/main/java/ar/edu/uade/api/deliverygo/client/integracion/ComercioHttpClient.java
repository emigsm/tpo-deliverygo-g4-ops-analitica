package ar.edu.uade.api.deliverygo.client.integracion;
import ar.edu.uade.api.deliverygo.client.ComercioClient;
import ar.edu.uade.api.deliverygo.dto.ComercioDTO;
import ar.edu.uade.api.deliverygo.excepcion.ExternalServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.time.Duration;
import java.util.Optional;

/**
 * DM-11: cliente HTTP al módulo Comercios/Catálogo (INT-03), activo solo con el perfil integracion. Endpoint público, sin API Key.
 * DM-20: consume GET /api/v1/comercios/{id}, la ruta publicada en el contrato de Catálogo (openapi-catalogo.yaml).
 * Usa RestClient con timeouts de 2 s de conexión y 3 s de lectura.
 * Traduce estado = ACTIVO al booleano activo de ComercioDTO, 404 a Optional.empty() y cualquier otro error o falla de red
 * a ExternalServiceUnavailableException.
 */
@Component
@Profile("integracion")
public class ComercioHttpClient implements ComercioClient {

    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final RestClient restClient;

    public ComercioHttpClient(@Value("${integraciones.comercios.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public Optional<ComercioDTO> obtenerComercio(Long comercioId) {
        try {
            ComercioResponse response = restClient.get()
                    .uri("/api/v1/comercios/{id}", comercioId)//actualizo a comercios para poder respetar el contrato de m-catalogo
                    .retrieve()
                    .body(ComercioResponse.class);

            if (response == null) {
                throw new ExternalServiceUnavailableException("Comercios devolvió una respuesta vacía");
            }
            return Optional.of(new ComercioDTO(response.id(), ESTADO_ACTIVO.equals(response.estado())));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new ExternalServiceUnavailableException("Comercios respondió ");
        } catch (ResourceAccessException e) {
            throw new ExternalServiceUnavailableException("Comercios no está disponible");
        }
    }

    // Subconjunto de ComercioResponseDTO de Catálogo; los campos extra se ignoran.
    private record ComercioResponse(Long id, String estado) {}
}