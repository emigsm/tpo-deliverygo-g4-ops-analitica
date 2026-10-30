package ar.edu.uade.api.deliverygo.dto;

import ar.edu.uade.api.deliverygo.model.FuenteMetrica;
import ar.edu.uade.api.deliverygo.model.TipoMetrica;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO-10 (salida, API-04) Representa una métrica sin ID ni fechaCalculo
@Schema(description = "Resumen de una métrica del tablero")
public class MetricaResumen {

    @Schema(description = "Fuente de los datos", example = "RESENAS")
    private FuenteMetrica fuente;
    @Schema(description = "Tipo de métrica", example = "CANTIDAD_RESENAS")
    private TipoMetrica tipoMetrica;
    @Schema(description = "Inicio del período incluido", example = "2025-01-01")
    private LocalDate periodoDesde;
    @Schema(description = "Fin del período incluido", example = "2025-01-31")
    private LocalDate periodoHasta;
    @Schema(description = "Valor de la métrica; null exclusivamente para PROMEDIO_CALIFICACION sin reseñas. Ese promedio no se persiste.", nullable = true, example = "25.00")
    private BigDecimal valor;

    public MetricaResumen() {
    }

    public MetricaResumen(FuenteMetrica fuente, TipoMetrica tipoMetrica, LocalDate periodoDesde,
                          LocalDate periodoHasta, BigDecimal valor) {
        this.fuente = fuente;
        this.tipoMetrica = tipoMetrica;
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
