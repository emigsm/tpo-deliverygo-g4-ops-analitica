package ar.edu.uade.api.deliverygo.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

//Respuesta para errores devuelto por GlobalExceptionHandler
@Schema(description = "Formato de error devuelto por el manejador global de excepciones")
public class ErrorResponseDTO {

    @Schema(description = "Fecha y hora del error", example = "2025-01-31T14:30:00")
    private LocalDateTime timestamp;
    @Schema(description = "Código HTTP", example = "400")
    private int status;
    @Schema(description = "Descripción del estado HTTP", example = "Bad Request")
    private String error;
    @Schema(description = "Detalle del error", example = "La puntuación debe estar entre 1 y 5")
    private String mensaje;
    @Schema(description = "Ruta solicitada", example = "/api/v1/reviews")
    private String path;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String mensaje, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
