package ar.edu.uade.api.deliverygo.model;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.time.LocalDateTime;

/**
 * ENT-01: reseña de un usuario sobre un pedido entregado (tabla resenas).
 * RN-02: una reseña por usuario, pedido y tipo de objetivo (uk_resena_usuario_pedido_tipo y control previo en ReviewService).
 * RN-04: puntuación de 1 a 5 (@Check en la BD y validación en ReviewService).
 * RN-05: inmutable, con updatable = false en cada campo y sin endpoints PUT ni DELETE.
 */
@Entity
@Table(name = "resenas", uniqueConstraints = @UniqueConstraint(name = "uk_resena_usuario_pedido_tipo", columnNames = {"usuario_id", "pedido_id", "tipo_objetivo"}))
@Check(name = "ck_resena_puntuacion", constraints = "puntuacion BETWEEN 1 AND 5")
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DM-06: usuario referenciado por id, sin FK
    @Column(nullable = false, updatable = false)
    private Long usuarioId;

    // DM-06: pedido referenciado por id, sin FK
    @Column(nullable = false, updatable = false)
    private Long pedidoId;

    // DM-01: COMERCIO o REPARTIDOR. EnumType.STRING guarda el nombre en la BD
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoObjetivo tipoObjetivo;

    // DM-06: id del comercio o del repartidor, sin FK
    @Column(nullable = false, updatable = false)
    private Long objetivoId;

    // RN-04: de 1 a 5
    @Column(nullable = false, updatable = false)
    private Integer puntuacion;

    // Opcional, hasta 1000 caracteres
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
