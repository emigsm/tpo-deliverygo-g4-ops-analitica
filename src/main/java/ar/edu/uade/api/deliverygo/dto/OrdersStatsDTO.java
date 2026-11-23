package ar.edu.uade.api.deliverygo.dto;

import java.time.LocalDate;

/**
 * Agregado de pedidos por estado y período (INT-05, contrato propuesto, pendiente de confirmar con Pedidos).
 *
 * No trae pedidos individuales: solo la cantidad. {@code AnalyticsService} la guarda como snapshot PEDIDOS_ENTREGADOS
 * y valida que {@code estado} y el período coincidan con lo pedido.
 */
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
