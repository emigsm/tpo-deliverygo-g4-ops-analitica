package ar.edu.uade.api.deliverygo.dto;

/**
 * Usuario recibido de Usuarios (INT-02, DTO-06). Solo tiene {@code id} y {@code activo}.
 * {@code ReviewService} lo usa para confirmar que el usuario existe y está activo antes de crear la reseña (RN-08).
 */
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
