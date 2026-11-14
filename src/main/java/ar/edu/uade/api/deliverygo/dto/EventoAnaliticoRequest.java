package ar.edu.uade.api.deliverygo.dto;


import ar.edu.uade.api.deliverygo.model.ModuloOrigen;
import ar.edu.uade.api.deliverygo.model.TipoEvento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO-11 (entrada, API-05). RN-09: tipoEvento, moduloOrigen, entidadId y fechaEvento son obligatorios.
// No declara Bean Validation: RN-09 se valida en SRV-04 para responder con ErrorResponseDTO.
public class EventoAnaliticoRequest {

    private TipoEvento tipoEvento;
    private ModuloOrigen moduloOrigen;
    private Long entidadId;
    private BigDecimal valor;          // opcional (ej. minutos de una entrega)
    private String detalle;            // opcional (ej. nuevo estado del pedido)
    private LocalDateTime fechaEvento;

    public EventoAnaliticoRequest() {}

    public EventoAnaliticoRequest(TipoEvento tipoEvento, ModuloOrigen moduloOrigen, Long entidadId,
                                  BigDecimal valor, String detalle, LocalDateTime fechaEvento) {
        this.tipoEvento = tipoEvento;
        this.moduloOrigen = moduloOrigen;
        this.entidadId = entidadId;
        this.valor = valor;
        this.detalle = detalle;
        this.fechaEvento = fechaEvento;
    }

    public TipoEvento getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoEvento tipoEvento) { this.tipoEvento = tipoEvento; }
    public ModuloOrigen getModuloOrigen() { return moduloOrigen; }
    public void setModuloOrigen(ModuloOrigen moduloOrigen) { this.moduloOrigen = moduloOrigen; }
    public Long getEntidadId() { return entidadId; }
    public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }
}
