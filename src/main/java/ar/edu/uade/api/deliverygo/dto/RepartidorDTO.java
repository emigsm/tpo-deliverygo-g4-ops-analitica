package ar.edu.uade.api.deliverygo.dto;

/**
 * Repartidor recibido de Logística (INT-04, DTO-08). Solo tiene {@code id} y {@code activo}, por la misma razón que {@code ComercioDTO}.
 * Lo consume {@code ReviewService} cuando la reseña es de tipo REPARTIDOR.
 */
// DTO-08 (entrada, INT-04).
public class RepartidorDTO {

    private Long id;
    private Boolean activo;

    public RepartidorDTO() {
    }

    public RepartidorDTO(Long id, Boolean activo) {
        this.id = id;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
