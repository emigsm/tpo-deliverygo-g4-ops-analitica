package ar.edu.uade.api.deliverygo.Dto;

import ar.edu.uade.api.deliverygo.model.TipoObjetivo;

import java.time.LocalDate;

public class ReseniaResponse {
    private Long id;
    private Long usuarioId;
    private TipoObjetivo tipoObjetivo;
    private int puntuacion;
    private String conmentario;
    private LocalDate fechaCreacion;


    public ReseniaResponse(Long usuarioId, TipoObjetivo tipoObjetivo, int puntuacion, String conmentario, LocalDate fechaCreacion) {
        this.usuarioId = usuarioId;
        this.tipoObjetivo = tipoObjetivo;
        this.puntuacion = puntuacion;
        this.conmentario = conmentario;
        this.fechaCreacion = fechaCreacion;
    }
}
