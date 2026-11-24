package ar.edu.uade.api.deliverygo.dto;
import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO-04 (salida, API-04): tablero de analítica de un período. generadoEn es el momento de la respuesta y metricas es una lista de DTO-10.
 * DM-09: parcial = true indica que se usó el snapshot del período exacto porque Pedidos no respondió o devolvió datos inválidos.
 */
@Schema(description = "Resultado del tablero de analítica para un período")
public class DashboardResponse {

    @Schema(type = "string", format = "local-date-time", description = "Fecha y hora de generación de la respuesta; no indica la antigüedad del snapshot de respaldo", example = "2025-01-31T14:30:00")
    private LocalDateTime generadoEn;
    @Schema(description = "Métricas disponibles para el período")
    private List<ar.edu.uade.api.deliverygo.dto.MetricaResumen> metricas;
    @Schema(description = "Indica uso de un snapshot del período exacto ante indisponibilidad o respuesta inválida de Pedidos", example = "false")
    private boolean parcial; // DM-09: true si se usó un snapshot de respaldo

    public DashboardResponse() {
    }

    public DashboardResponse(LocalDateTime generadoEn, List<ar.edu.uade.api.deliverygo.dto.MetricaResumen> metricas, boolean parcial) {
        this.generadoEn = generadoEn;
        this.metricas = metricas;
        this.parcial = parcial;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(LocalDateTime generadoEn) {
        this.generadoEn = generadoEn;
    }

    public List<ar.edu.uade.api.deliverygo.dto.MetricaResumen> getMetricas() {
        return metricas;
    }

    public void setMetricas(List<ar.edu.uade.api.deliverygo.dto.MetricaResumen> metricas) {
        this.metricas = metricas;
    }

    public boolean isParcial() {
        return parcial;
    }

    public void setParcial(boolean parcial) {
        this.parcial = parcial;
    }
}
