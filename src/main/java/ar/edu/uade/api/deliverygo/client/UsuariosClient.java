package ar.edu.uade.api.deliverygo.client;

import ar.edu.uade.api.deliverygo.dto.UsuarioDTO;

import java.util.Optional;

//Adapter a Usuarios (DM-11)
public interface UsuariosClient {

    //GET /api/v1/users/{id} (INT-02) Devuelve 200, 404, 503
    Optional<UsuarioDTO> obtenerUsuario(Long usuarioId);
}
