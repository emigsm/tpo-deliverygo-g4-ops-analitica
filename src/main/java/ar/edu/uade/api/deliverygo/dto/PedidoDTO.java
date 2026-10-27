package ar.edu.uade.api.deliverygo.dto;

// DTO-05 (entrada, INT-01).
public class PedidoDTO {

    private Long id;
    private Long usuarioId;
    private Long comercioId;
    private Long repartidorId;
    private String estado;

    public PedidoDTO() {
    }

    public PedidoDTO(Long id, Long usuarioId, Long comercioId, Long repartidorId, String estado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.comercioId = comercioId;
        this.repartidorId = repartidorId;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getComercioId() {
        return comercioId;
    }

    public void setComercioId(Long comercioId) {
        this.comercioId = comercioId;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
