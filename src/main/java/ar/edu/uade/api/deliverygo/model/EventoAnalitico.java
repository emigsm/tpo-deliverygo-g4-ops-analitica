package ar.edu.uade.api.deliverygo.model;

// ENT-03: evento operativo genérico emitido por otro módulo (DM-14).
// Sin relación persistente con Metrica ni con Resena: el servicio lee eventos,
// calcula y guarda el resultado como Metrica.

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_analiticos")
public class EventoAnalitico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RN-09
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipoEvento;

    // RN-09, DM-15
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuloOrigen moduloOrigen;

    // RN-09. Referencia externa sin FK (DM-06): pedido, pago o entrega que originó el evento.
    // Long provisional: el tipo definitivo está pendiente de acordar.
    @Column(nullable = false)
    private Long entidadId;

    // Opcional: magnitud numérica cuando aplica (ej. minutos de una entrega)
    @Column(precision = 19, scale = 4)
    private BigDecimal valor;

    // Opcional: dato cualitativo cuando aplica (ej. nuevo estado del pedido)
    private String detalle;

    // RN-09. LocalDateTime provisional: zona horaria pendiente de acordar.
    @Column(nullable = false)
    private LocalDateTime fechaEvento;

    // JPA exige un constructor vacío
    public EventoAnalitico() {
    }

    public EventoAnalitico(TipoEvento tipoEvento, ModuloOrigen moduloOrigen, Long entidadId,
                           BigDecimal valor, String detalle, LocalDateTime fechaEvento) {
        this.tipoEvento = tipoEvento;
        this.moduloOrigen = moduloOrigen;
        this.entidadId = entidadId;
        this.valor = valor;
        this.detalle = detalle;
        this.fechaEvento = fechaEvento;
    }

    public Long getId() { return id; }
    public TipoEvento getTipoEvento() { return tipoEvento; }
    public ModuloOrigen getModuloOrigen() { return moduloOrigen; }
    public Long getEntidadId() { return entidadId; }
    public BigDecimal getValor() { return valor; }
    public String getDetalle() { return detalle; }
    public LocalDateTime getFechaEvento() { return fechaEvento; }
}
