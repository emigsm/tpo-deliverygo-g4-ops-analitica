package ar.edu.uade.api.deliverygo.dto;

import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

//Respuesta para errores devuelto por GlobalExceptionHandler
@Schema(description = "Formato de error devuelto por el manejador global de excepciones",
        requiredProperties = {"timestamp", "status", "code", "message", "path", "correlationId"})
public class ErrorResponseDTO {

    @Schema(description = "Fecha y hora UTC del error", example = "2026-10-30T12:00:00Z")
    private Instant timestamp;
    @Schema(description = "Código HTTP", example = "400")
    private int status;
    @Schema(description = "Código estable del error", example = "INVALID_RATING")
    private String code;
    @Schema(description = "Detalle del error", example = "La puntuación debe estar entre 1 y 5")
    private String message;
    @Schema(description = "Ruta solicitada", example = "/api/v1/reviews")
    private String path;

    @Schema(description = "UUID de X-Correlation-Id válido o generado por el servidor", format = "uuid")
    private String correlationId;

    public String getCorrelationId() { return correlationId; }

    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(Instant timestamp, int status, String code, String message, String path, String correlationId) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
