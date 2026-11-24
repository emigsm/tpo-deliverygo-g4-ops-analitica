package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.UsuariosClient;
import ar.edu.uade.api.deliverygo.dto.UsuarioDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * DM-11: mock de Usuarios (INT-02), activo fuera del perfil integracion.
 * Datos fijos: el usuario 100 y el 200 existen y están activos; el 300 existe pero está inactivo (RN-08).
 */
@Component
@Profile("!integracion")
public class UsuariosClientMock implements UsuariosClient {

    private final Map<Long, UsuarioDTO> usuarios = Map.of(
            100L, new UsuarioDTO(100L, true),
            200L, new UsuarioDTO(200L, true),
            300L, new UsuarioDTO(300L, false));

    @Override
    public Optional<UsuarioDTO> obtenerUsuario(Long usuarioId) {
        return Optional.ofNullable(usuarios.get(usuarioId));
    }
}
