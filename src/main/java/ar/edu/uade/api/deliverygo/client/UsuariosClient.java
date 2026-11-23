package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.UsuarioDTO;

import java.util.Optional;

/**
 * Puerto (adapter, DM-11) hacia el módulo Usuarios (INT-02).
 */
//Adapter a Usuarios (DM-11)
public interface UsuariosClient {

    //GET /api/v1/users/{id} (INT-02) Devuelve 200, 404, 503
    Optional<UsuarioDTO> obtenerUsuario(Long usuarioId);
}
