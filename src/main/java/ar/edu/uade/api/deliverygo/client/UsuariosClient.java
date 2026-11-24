package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.UsuarioDTO;

import java.util.Optional;

/**
 * DM-11: adapter hacia el módulo Usuarios (INT-02). Perfil integracion: UsuariosHttpClient. Resto: UsuariosClientMock.
 */
public interface UsuariosClient {

    // INT-02 (RN-08): devuelve el usuario, o vacío si no existe (404).
    // Si el módulo no responde lanza ExternalServiceUnavailableException (503)
    Optional<UsuarioDTO> obtenerUsuario(Long usuarioId);
}
