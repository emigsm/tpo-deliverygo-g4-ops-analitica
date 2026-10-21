package ar.edu.uade.api.deliverygo.dto;

// DTO-07 (entrada, INT-03).
public class ComercioDTO {

    private Long id;
    private Boolean activo;

    public ComercioDTO() {
    }

    public ComercioDTO(Long id, Boolean activo) {
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
