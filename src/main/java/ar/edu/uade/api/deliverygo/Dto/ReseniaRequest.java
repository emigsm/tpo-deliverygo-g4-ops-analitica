package ar.edu.uade.api.deliverygo.Dto;

import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ReseniaRequest {
    private Long usuarioId;
    private Long pedidoId;
    private TipoObjetivo tipoObjetivo;
    private  Long objetivoId;
    @Min(1) @Max(5)
    private int punticion;
    private String comentario;


    public ReseniaRequest(Long usuarioId, Long pedidoId, TipoObjetivo tipoObjetivo, Long objetivoId, int punticion, String comentario) {
        this.usuarioId = usuarioId;
        this.pedidoId = pedidoId;
        this.tipoObjetivo = tipoObjetivo;
        this.objetivoId = objetivoId;
        this.punticion = punticion;
        this.comentario = comentario;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public TipoObjetivo getTipoObjetivo() {
        return tipoObjetivo;
    }

    public void setTipoObjetivo(TipoObjetivo tipoObjetivo) {
        this.tipoObjetivo = tipoObjetivo;
    }

    public Long getObjetivoId() {
        return objetivoId;
    }

    public void setObjetivoId(Long objetivoId) {
        this.objetivoId = objetivoId;
    }

    public int getPunticion() {
        return punticion;
    }

    public void setPunticion(int punticion) {
        this.punticion = punticion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
