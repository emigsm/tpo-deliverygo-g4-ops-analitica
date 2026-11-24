package ar.edu.uade.api.deliverygo.dto;

import ar.edu.uade.api.deliverygo.model.FuenteMetrica;
import ar.edu.uade.api.deliverygo.model.OrigenMetrica;
import ar.edu.uade.api.deliverygo.model.TipoMetrica;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO-10 (salida, API-04): una métrica del tablero, sin id ni fechaCalculo (detalles internos de ENT-02).
 * origen y formula se obtienen de TipoMetrica: dicen de dónde sale el dato y cómo se calcula.
 * valor es null en PROMEDIO_CALIFICACION sin reseñas y en TIEMPO_PROMEDIO_ENTREGA sin eventos con valor. Esos casos no se persisten porque Metrica.valor es NOT NULL.
 */
@Schema(description = "Resumen de una métrica del tablero")
public class MetricaResumen {

    @Schema(description = "Fuente de los datos", example = "RESENAS")
    private FuenteMetrica fuente;
    @Schema(description = "Tipo de métrica", example = "CANTIDAD_RESENAS")
    private TipoMetrica tipoMetrica;
    @Schema(description = "De dónde sale el dato: la tabla de reseñas propia o eventos enviados por otros módulos", example = "RESENAS_PROPIAS")
    private OrigenMetrica origen;
    @Schema(description = "Cómo se calcula el valor", example = "Cantidad de reseñas creadas en el período")
    private String formula;
    @Schema(description = "Inicio del período incluido", example = "2025-01-01")
    private LocalDate periodoDesde;
    @Schema(description = "Fin del período incluido", example = "2025-01-31")
    private LocalDate periodoHasta;
    @Schema(description = "Valor de la métrica; null para PROMEDIO_CALIFICACION sin reseñas y para TIEMPO_PROMEDIO_ENTREGA sin eventos con valor. Esos casos no se persisten.", nullable = true, example = "25.00")
    private BigDecimal valor;

    public MetricaResumen() {
    }

    public MetricaResumen(FuenteMetrica fuente, TipoMetrica tipoMetrica, LocalDate periodoDesde,
                          LocalDate periodoHasta, BigDecimal valor) {
        this.fuente = fuente;
        this.tipoMetrica = tipoMetrica;
        this.origen = tipoMetrica.getOrigen();
        this.formula = tipoMetrica.getFormula();
        this.periodoDesde = periodoDesde;
        this.periodoHasta = periodoHasta;
        this.valor = valor;
    }

    public FuenteMetrica getFuente() {
        return fuente;
    }

    public void setFuente(FuenteMetrica fuente) {
        this.fuente = fuente;
    }

    public TipoMetrica getTipoMetrica() {
        return tipoMetrica;
    }

    public void setTipoMetrica(TipoMetrica tipoMetrica) {
        this.tipoMetrica = tipoMetrica;
        this.origen = tipoMetrica.getOrigen();
        this.formula = tipoMetrica.getFormula();
    }

    public OrigenMetrica getOrigen() {
        return origen;
    }

    public String getFormula() {
        return formula;
    }

    public LocalDate getPeriodoDesde() {
        return periodoDesde;
    }

    public void setPeriodoDesde(LocalDate periodoDesde) {
        this.periodoDesde = periodoDesde;
    }

    public LocalDate getPeriodoHasta() {
        return periodoHasta;
    }

    public void setPeriodoHasta(LocalDate periodoHasta) {
        this.periodoHasta = periodoHasta;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
