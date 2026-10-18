package ar.edu.uade.api.deliverygo.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// RN-05: Una reseña es inmutable una vez creada. No hay endpoints PUT/DELETE
@Entity
@Table(name = "resenas", uniqueConstraints = @UniqueConstraint(name = "uk_resena_usuario_pedido_tipo", columnNames = {"usuario_id", "pedido_id", "tipo_objetivo"}))
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // referencia a Usuario, sin FK (DM-06)
    @Column(nullable = false, updatable = false)
    private Long usuarioId;

    // DM-06: referencia a Pedido, sin FK
    @Column(nullable = false, updatable = false)
    private Long pedidoId;

    // DM-01: Enum que diferencia entre comercio y repartidor.
    // Enumerated(EnumType.STRING) permite que JPA persista el literal del Enum.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoObjetivo tipoObjetivo;

    // DM-06: comercioId o repartidorId
    @Column(nullable = false, updatable = false)
    private Long objetivoId;

    // RN-04: De 1 a 5 (validar en ReviewService)
    @Column(nullable = false, updatable = false)
    private Integer puntuacion;

    // Comentario opcional de la reseña
    @Column(length = 1000, updatable = false)
    private String comentario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public Resena() {
    }

    public Resena(Long usuarioId, Long pedidoId, TipoObjetivo tipoObjetivo, Long objetivoId, Integer puntuacion, String comentario) {
        this.usuarioId = usuarioId;
        this.pedidoId = pedidoId;
        this.tipoObjetivo = tipoObjetivo;
        this.objetivoId = objetivoId;
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public TipoObjetivo getTipoObjetivo() {
        return tipoObjetivo;
    }

    public Long getObjetivoId() {
        return objetivoId;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    @Override
    public String toString() {
        return "Resena{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", pedidoId=" + pedidoId +
                ", tipoObjetivo=" + tipoObjetivo +
                ", objetivoId=" + objetivoId +
                ", puntuacion=" + puntuacion +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}