package ar.edu.uade.api.deliverygo.dto;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

// DTO-02 (salida, API-01)
@Schema(description = "Reseña creada")
public class ResenaResponse {

    @Schema(description = "Identificador de la reseña", example = "9001")
    private Long id;
    @Schema(description = "Identificador del usuario que realizó la reseña", example = "100")
    private Long usuarioId;
    @Schema(description = "Identificador del pedido reseñado", example = "501")
    private Long pedidoId;
    @Schema(description = "Tipo de objetivo reseñado", example = "COMERCIO")
    private TipoObjetivo tipoObjetivo;
    @Schema(description = "Identificador del comercio o repartidor derivado del pedido", example = "10")
    private Long objetivoId;
    @Schema(description = "Puntuación otorgada", example = "5")
    private Integer puntuacion;
    @Schema(description = "Comentario de la reseña", example = "Entrega rápida y atención cordial")
    private String comentario;
    @Schema(description = "Fecha y hora de creación", example = "2025-01-31T14:30:00")
    private LocalDateTime fechaCreacion;

    public ResenaResponse() {
    }

    public ResenaResponse(Long id, Long usuarioId, Long pedidoId, TipoObjetivo tipoObjetivo, Long objetivoId, Integer puntuacion, String comentario, LocalDateTime fechaCreacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.pedidoId = pedidoId;
        this.tipoObjetivo = tipoObjetivo;
        this.objetivoId = objetivoId;
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fechaCreacion = fechaCreacion;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
