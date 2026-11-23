package ar.edu.uade.api.deliverygo.client.integracion;
import ar.edu.uade.api.deliverygo.client.UsuariosClient;
import ar.edu.uade.api.deliverygo.dto.UsuarioDTO;
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
 * Cliente HTTP a Usuarios (INT-02), activo solo con el perfil {@code integracion}.
 *
 * Igual que {@code ComercioHttpClient} en timeouts y mapeo de errores, con una diferencia: envía la clave
 * en el header {@code X-Api-Key}, configurada con la propiedad {@code integraciones.usuarios.api-key}.
 *
 * Observación: esa propiedad no está en {@code application.properties}. Debe venir de otro archivo o variable de entorno.
 */
// Adapter HTTP a Usuarios (INT-02). Traduce estado (ACTIVO/INACTIVO) a activo (DM-11).
@Component
@Profile("integracion")
public class UsuariosHttpClient implements UsuariosClient {

    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final RestClient restClient;

    public UsuariosHttpClient(@Value("${integraciones.usuarios.url}") String baseUrl,
                              @Value("${integraciones.usuarios.api-key}") String apiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuario(Long usuarioId) {
        try {
            UsuarioResponse response = restClient.get()
                    .uri("/api/v1/users/{id}", usuarioId)
                    .retrieve()
                    .body(UsuarioResponse.class);

            if (response == null) {
                throw new ExternalServiceUnavailableException("Usuarios devolvió una respuesta vacía");
            }
            return Optional.of(new UsuarioDTO(response.id(), ESTADO_ACTIVO.equals(response.estado())));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new ExternalServiceUnavailableException("Usuarios respondió " );
        } catch (ResourceAccessException e) {
            throw new ExternalServiceUnavailableException("Usuarios no está disponible");
        }
    }

    // Subconjunto de UsuarioResponse de Usuarios; los campos extra se ignoran.
    private record UsuarioResponse(Long id, String estado) {}
}