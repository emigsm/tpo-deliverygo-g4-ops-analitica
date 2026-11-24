package ar.edu.uade.api.deliverygo.mapper;


import ar.edu.uade.api.deliverygo.dto.EventoAnaliticoRequest;
import ar.edu.uade.api.deliverygo.dto.EventoAnaliticoResponse;
import ar.edu.uade.api.deliverygo.model.EventoAnalitico;

/**
 * ENT-03 <-> DTO-11 y DTO-12: convierte el request en entidad y la entidad en response.
 * Mapeo manual y sin estado: es trivial y el pom no incluye MapStruct.
 */
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
