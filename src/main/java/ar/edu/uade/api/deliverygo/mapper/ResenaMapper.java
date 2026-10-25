package ar.edu.uade.api.deliverygo.mapper;

import ar.edu.uade.api.deliverygo.dto.ResenaRequest;
import ar.edu.uade.api.deliverygo.dto.ResenaResponse;
import ar.edu.uade.api.deliverygo.model.Resena;

// Mapper entre Resena y (ENT-01) y sus DTOs (DTO-01, DTO-02)
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
