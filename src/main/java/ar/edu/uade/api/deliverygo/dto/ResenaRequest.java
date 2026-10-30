package ar.edu.uade.api.deliverygo.dto;

import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import io.swagger.v3.oas.annotations.media.Schema;

// DTO-01 (entrada, API-01)
// DM-13: usuarioId sale de JWT, objetivoId de PedidoDTO segun tipoObjetivo
@Schema(description = "Datos de entrada para registrar una reseña. No declara restricciones Bean Validation; la puntuación se valida en el servicio.")
public class ResenaRequest {

    @Schema(description = "Identificador del pedido sobre el que se realiza la reseña", example = "501")
    private Long pedidoId;
    @Schema(description = "Tipo de objetivo de la reseña", example = "COMERCIO")
    private TipoObjetivo tipoObjetivo;
    @Schema(description = "Puntuación de la reseña. La aplicación rechaza valores nulos o fuera del rango 1 a 5.", example = "5", minimum = "1", maximum = "5")
    private Integer puntuacion;
    @Schema(description = "Comentario opcional de la reseña", example = "Entrega rápida y atención cordial")
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
