package ar.edu.uade.api.deliverygo.mapper;

import ar.edu.uade.api.deliverygo.dto.ResenaRequest;
import ar.edu.uade.api.deliverygo.dto.ResenaResponse;
import ar.edu.uade.api.deliverygo.model.Resena;

/**
 * ENT-01 <-> DTO-01 y DTO-02. DM-13: toEntity recibe usuarioId (del JWT) y objetivoId (derivado del pedido)
 * como argumentos, porque no vienen en el body.
 */
public final class ResenaMapper {

    private ResenaMapper() {
        // Clase de utilidad no se instancia.
    }

    public static Resena toEntity(ResenaRequest resenaRequest, Long usuarioIdAutenticado, Long objetivoId) {
        return new Resena(usuarioIdAutenticado, resenaRequest.getPedidoId(), resenaRequest.getTipoObjetivo(), objetivoId, resenaRequest.getPuntuacion(), resenaRequest.getComentario());
    }

    public static ResenaResponse toResponse(Resena resena) {
        return new ResenaResponse(resena.getId(), resena.getUsuarioId(), resena.getPedidoId(), resena.getTipoObjetivo(), resena.getObjetivoId(), resena.getPuntuacion(), resena.getComentario(), resena.getFechaCreacion());
    }
}
