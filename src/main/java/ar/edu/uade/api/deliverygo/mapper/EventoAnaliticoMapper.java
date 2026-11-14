package ar.edu.uade.api.deliverygo.mapper;


import ar.edu.uade.api.deliverygo.dto.EventoAnaliticoRequest;
import ar.edu.uade.api.deliverygo.dto.EventoAnaliticoResponse;
import ar.edu.uade.api.deliverygo.model.EventoAnalitico;

// Convierte entre EventoAnalitico (ENT-03) y sus DTOs (DTO-11, DTO-12)
public class EventoAnaliticoMapper {
    private EventoAnaliticoMapper() {
        // Clase de utilidad: no se instancia
    }

    // DTO-11 -> entidad
    public static EventoAnalitico toEntity(EventoAnaliticoRequest request) {
        return new EventoAnalitico(
                request.getTipoEvento(),
                request.getModuloOrigen(),
                request.getEntidadId(),
                request.getValor(),
                request.getDetalle(),
                request.getFechaEvento());
    }

    // entidad -> DTO-12
    public static EventoAnaliticoResponse toResponse(EventoAnalitico evento) {
        return new EventoAnaliticoResponse(
                evento.getId(),
                evento.getTipoEvento(),
                evento.getModuloOrigen(),
                evento.getEntidadId(),
                evento.getValor(),
                evento.getDetalle(),
                evento.getFechaEvento());
    }

}
