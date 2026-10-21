package ar.edu.uade.api.deliverygo.dto;

// DTO-06 (entrada, INT-02).
public class UsuarioDTO {

    private Long id;
    private Boolean activo;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Long id, Boolean activo) {
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
