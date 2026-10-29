package ar.edu.uade.api.deliverygo.dto;
import java.time.LocalDateTime;
import java.util.List;

// DTO-04 (salida, API-04)
public class DashboardResponse {

    private LocalDateTime generadoEn;
    private List<ar.edu.uade.api.deliverygo.dto.MetricaResumen> metricas;
    private boolean parcial; //DM-09

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
