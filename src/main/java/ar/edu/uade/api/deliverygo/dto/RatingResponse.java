package ar.edu.uade.api.deliverygo.dto;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

// DTO-03 (salida, API-02/API-03)
@Schema(description = "Calificación agregada de un comercio o repartidor")
public class RatingResponse {

    @Schema(description = "Identificador del objetivo", example = "10")
    private Long objetivoId;
    @Schema(description = "Tipo de objetivo", example = "COMERCIO")
    private TipoObjetivo tipoObjetivo;
    @Schema(description = "Promedio con dos decimales; null cuando no hay reseñas", nullable = true, example = "4.75")
    private BigDecimal promedio;
    @Schema(description = "Cantidad de reseñas consideradas; cero cuando no hay reseñas", example = "12")
    private Long cantidadResenas;

    public RatingResponse() {
    }

    public RatingResponse(Long objetivoId, TipoObjetivo tipoObjetivo, BigDecimal promedio, Long cantidadResenas) {
        this.objetivoId = objetivoId;
        this.tipoObjetivo = tipoObjetivo;
        this.promedio = promedio;
        this.cantidadResenas = cantidadResenas;
    }

    public Long getObjetivoId() {
        return objetivoId;
    }

    public void setObjetivoId(Long objetivoId) {
        this.objetivoId = objetivoId;
    }

    public TipoObjetivo getTipoObjetivo() {
        return tipoObjetivo;
    }

    public void setTipoObjetivo(TipoObjetivo tipoObjetivo) {
        this.tipoObjetivo = tipoObjetivo;
    }

    public BigDecimal getPromedio() {
        return promedio;
    }

    public void setPromedio(BigDecimal promedio) {
        this.promedio = promedio;
    }

    public Long getCantidadResenas() {
        return cantidadResenas;
    }

    public void setCantidadResenas(Long cantidadResenas) {
        this.cantidadResenas = cantidadResenas;
    }
}
