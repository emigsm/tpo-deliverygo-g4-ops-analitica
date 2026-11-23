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
 * Cliente HTTP a Catálogo (INT-03), activo solo con el perfil {@code integracion}.
 *
 * Decisiones:
 * <ul>
 *   <li>{@link org.springframework.web.client.RestClient} (API síncrona de Spring 6.1+), con timeouts de 2 s de conexión y 3 s de lectura para no bloquear hilos de forma indefinida.</li>
 *   <li>{@code estado = ACTIVO} se traduce al booleano {@code activo} de {@code ComercioDTO}. El resto de los campos se ignora.</li>
 *   <li>404 se traduce a {@code Optional.empty()}. Otros errores HTTP y fallos de red se traducen a {@code ExternalServiceUnavailableException}.</li>
 * </ul>
 *
 * Observación: el path es {@code /api/v1/merchant/{id}} (singular). El mensaje de error de respuesta incompleta
 * termina con un espacio y no incluye el código HTTP.
 */
// Adapter HTTP a Catálogo (INT-03). Endpoint público, sin API Key.
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
                    .uri("/api/v1/merchant/{id}", comercioId)
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