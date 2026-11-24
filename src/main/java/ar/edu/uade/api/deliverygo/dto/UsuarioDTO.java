package ar.edu.uade.api.deliverygo.dto;

/**
 * DTO-06 (entrada, INT-02): usuario recibido de Usuarios, solo id y activo.
 * ReviewService lo usa para confirmar que existe y está activo (RN-08).
 */
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
