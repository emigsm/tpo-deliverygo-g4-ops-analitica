package ar.edu.uade.api.deliverygo.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * ENT-02: resultado ya calculado de un indicador del tablero para una fuente y un período (tabla metricas, CU-05).
 * DM-05: se guarda como snapshot en lugar de recalcularse en cada consulta.
 * RN-06: no se repite la misma fuente, tipo de métrica y período (uk_metrica_fuente_tipo_periodo).
 * Solo valor y fechaCalculo tienen setter: al recalcular un período se actualiza la fila existente.
 */
@Entity
@Table(name = "metricas", uniqueConstraints = @UniqueConstraint(name = "uk_metrica_fuente_tipo_periodo", columnNames = {"fuente", "tipo_metrica", "periodo_desde", "periodo_hasta"}))
public class Metrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RN-03: toda métrica indica su fuente
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuenteMetrica fuente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMetrica tipoMetrica;

    // RN-03: toda métrica indica su período
    @Column(nullable = false)
    private LocalDate periodoDesde;

    // RN-03: toda métrica indica su período
    @Column(nullable = false)
    private LocalDate periodoHasta;

    // BigDecimal con 4 decimales para no perder precisión
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime fechaCalculo;

    public Metrica() {
    }

    public Metrica(FuenteMetrica fuente, TipoMetrica tipoMetrica, LocalDate periodoDesde, LocalDate periodoHasta, BigDecimal valor, LocalDateTime fechaCalculo) {
        this.fuente = fuente;
        this.tipoMetrica = tipoMetrica;
        this.periodoDesde = periodoDesde;
        this.periodoHasta = periodoHasta;
        this.valor = valor;
        this.fechaCalculo = fechaCalculo;
    }

    public Long getId() {
        return id;
    }

    public FuenteMetrica getFuente() {
        return fuente;
    }

    public TipoMetrica getTipoMetrica() {
        return tipoMetrica;
    }

    public LocalDate getPeriodoDesde() {
        return periodoDesde;
    }

    public LocalDate getPeriodoHasta() {
        return periodoHasta;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDateTime fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }

    @Override
    public String toString() {
        return "Metrica{" +
                "id=" + id +
                ", fuente=" + fuente +
                ", tipoMetrica=" + tipoMetrica +
                ", periodoDesde=" + periodoDesde +
                ", periodoHasta=" + periodoHasta +
                ", valor=" + valor +
                ", fechaCalculo=" + fechaCalculo +
                '}';
    }
}
