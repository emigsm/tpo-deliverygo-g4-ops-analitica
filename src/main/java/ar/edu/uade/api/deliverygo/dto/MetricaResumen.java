package ar.edu.uade.api.deliverygo.dto;

import ar.edu.uade.api.deliverygo.model.FuenteMetrica;
import ar.edu.uade.api.deliverygo.model.TipoMetrica;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO-10 (salida, API-04) Representa una métrica sin ID ni fechaCalculo
public class MetricaResumen {

    private FuenteMetrica fuente;
    private TipoMetrica tipoMetrica;
    private LocalDate periodoDesde;
    private LocalDate periodoHasta;
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
