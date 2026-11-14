package ar.edu.uade.api.deliverygo.dto;

import ar.edu.uade.api.deliverygo.model.ModuloOrigen;
import ar.edu.uade.api.deliverygo.model.TipoEvento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO-12 (salida, API-05): el evento registrado. Es DTO-11 más el id generado.
public class EventoAnaliticoResponse {
    private Long id;
    private TipoEvento tipoEvento;
    private ModuloOrigen moduloOrigen;
    private Long entidadId;
    private BigDecimal valor;
    private String detalle;
    private LocalDateTime fechaEvento;

    public EventoAnaliticoResponse() {}

    public EventoAnaliticoResponse(Long id, TipoEvento tipoEvento, ModuloOrigen moduloOrigen, Long entidadId,
                                   BigDecimal valor, String detalle, LocalDateTime fechaEvento) {
        this.id = id;
        this.tipoEvento = tipoEvento;
        this.moduloOrigen = moduloOrigen;
        this.entidadId = entidadId;
        this.valor = valor;
        this.detalle = detalle;
        this.fechaEvento = fechaEvento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
