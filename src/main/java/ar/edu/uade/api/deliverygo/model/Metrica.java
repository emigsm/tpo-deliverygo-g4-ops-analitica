package ar.edu.uade.api.deliverygo.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

// Métrica: resultado agregado ya calculado del tablero.
@Entity
@Table(name = "metricas", uniqueConstraints = @UniqueConstraint(name = "uk_metrica_fuente_tipo_periodo", columnNames = {"fuente", "tipo_metrica", "periodo_desde", "periodo_hasta"}))
public class Metrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RN-03
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuenteMetrica fuente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMetrica tipoMetrica;

    // RN-03
    @Column(nullable = false)
    private LocalDate periodoDesde;

    // RN-03
    @Column(nullable = false)
    private LocalDate periodoHasta;

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