package ar.edu.uade.api.deliverygo.client.mock;

import ar.edu.uade.api.deliverygo.client.UsuariosClient;
import ar.edu.uade.api.deliverygo.dto.UsuarioDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Implementación de prueba de {@link ar.edu.uade.api.deliverygo.client.UsuariosClient}, activa fuera del perfil {@code integracion}.
 *
 * Datos fijos: el usuario {@code 100} existe y está activo.
 */
//Mock de Usuarios (DM-11)
//@Component para que Spring los inyecte como implementación de las interfaces
@Component
@Profile("!integracion")
public class UsuariosClientMock implements UsuariosClient {

    private final Map<Long, UsuarioDTO> usuarios = Map.of(100L, new UsuarioDTO(100L, true));

    @Override
    public Optional<UsuarioDTO> obtenerUsuario(Long usuarioId) {
        return Optional.ofNullable(usuarios.get(usuarioId));
    }
}
