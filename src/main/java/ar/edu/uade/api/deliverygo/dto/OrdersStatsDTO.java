package ar.edu.uade.api.deliverygo.dto;

import java.time.LocalDate;

// DTO-09 (entrada, INT-05).
public class OrdersStatsDTO {

    private String estado;
    private LocalDate periodoDesde;
    private LocalDate periodoHasta;
    private Long cantidad;

    public OrdersStatsDTO() {
    }

    public OrdersStatsDTO(String estado, LocalDate periodoDesde, LocalDate periodoHasta, Long cantidad) {
        this.estado = estado;
        this.periodoDesde = periodoDesde;
        this.periodoHasta = periodoHasta;
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getPeriodoDesde() {
        return periodoDesde;
    }

    public void setPeriodoDesde(LocalDate periodoDesde) {
        this.periodoDesde = periodoDesde;
    }

    public LocalDate getPeriodoHasta() {
        return periodoHasta;
    }

    public void setPeriodoHasta(LocalDate periodoHasta) {
        this.periodoHasta = periodoHasta;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }
}
