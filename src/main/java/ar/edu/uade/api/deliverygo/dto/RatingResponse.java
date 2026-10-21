package ar.edu.uade.api.deliverygo.dto;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import java.math.BigDecimal;

// DTO-03 (salida, API-02/API-03)
public class RatingResponse {

    private Long objetivoId;
    private TipoObjetivo tipoObjetivo;
    private BigDecimal promedio;
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
