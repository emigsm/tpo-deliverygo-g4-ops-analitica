package ar.edu.uade.api.deliverygo.dto;

import ar.edu.uade.api.deliverygo.model.TipoObjetivo;

// DTO-01 (entrada, API-01)
// DM-13: usuarioId sale de JWT, objetivoId de PedidoDTO segun tipoObjetivo
public class ResenaRequest {

    private Long pedidoId;
    private TipoObjetivo tipoObjetivo;
    private Integer puntuacion;
    private String comentario;

    public ResenaRequest() {
    }

    public ResenaRequest(Long pedidoId, TipoObjetivo tipoObjetivo, Integer puntuacion, String comentario) {
        this.pedidoId = pedidoId;
        this.tipoObjetivo = tipoObjetivo;
        this.puntuacion = puntuacion;
        this.comentario = comentario;
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

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
