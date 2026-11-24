package ar.edu.uade.api.deliverygo.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ENT-03: hecho operativo emitido por otro módulo (tabla eventos_analiticos). DM-14: entidad única con discriminador tipoEvento.
 * Es la entrada de CU-08, CU-09 y CU-10: AnalyticsService lee los eventos y guarda el resultado como Metrica.
 * No tiene relación JPA con Metrica ni con Resena.
 */
@Entity
@Table(name = "eventos_analiticos")
public class EventoAnalitico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RN-09, DM-14: indica qué ocurrió
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipoEvento;

    // RN-09, DM-15: módulo que emitió el evento
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuloOrigen moduloOrigen;

    // RN-09, DM-06: id del pedido, pago o entrega que originó el evento, sin FK
    @Column(nullable = false)
    private Long entidadId;

    // Opcional: magnitud numérica (ej. minutos de una entrega)
    @Column(precision = 19, scale = 4)
    private BigDecimal valor;

    // Opcional: dato de texto (ej. nuevo estado del pedido)
    private String detalle;

    // RN-09: cuándo ocurrió. Zona horaria pendiente de acordar entre módulos
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
