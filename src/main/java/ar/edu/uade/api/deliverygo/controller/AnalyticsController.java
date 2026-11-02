package ar.edu.uade.api.deliverygo.controller;

import ar.edu.uade.api.deliverygo.controller.OpinionesController.SpringBootErrorSchema;
import ar.edu.uade.api.deliverygo.dto.DashboardResponse;
import ar.edu.uade.api.deliverygo.dto.ErrorResponseDTO;
import ar.edu.uade.api.deliverygo.services.AnalyticsService;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }


    @Operation(summary = "Consultar tablero de analítica",
            description = "CU-05, RN-03 y RN-06: obtiene cantidad de reseñas, promedio de calificación y pedidos entregados. " +
                    "Cada fecha omitida toma la fecha actual del servidor; ambos extremos son inclusivos. " +
                    "Sin reseñas, el promedio es null y no se persiste. INT-05 es un contrato propuesto pendiente " +
                    "de confirmación con Pedidos. Si falla esa fuente, usa exclusivamente el snapshot del período exacto " +
                    "y responde con parcial=true; sin respaldo válido responde 503.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tablero generado; parcial=true indica uso de un snapshot de Pedidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período invertido o fecha con formato inválido. " +
                    "El período invertido usa ErrorResponseDTO; el formato inválido usa la respuesta estándar de Spring.",
                    content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {ErrorResponseDTO.class, SpringBootErrorSchema.class}))),
            @ApiResponse(responseCode = "503", description = "Pedidos no disponible o respuesta inválida, sin snapshot válido del período exacto",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<DashboardResponse> obtenerTablero(
            @Parameter(description = "Inicio inclusivo, ISO-8601 (yyyy-MM-dd). Si se omite, fecha actual del servidor",
                    example = "2025-01-01")
            @RequestParam(value = "periodoDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fin inclusivo, ISO-8601 (yyyy-MM-dd). Si se omite, fecha actual del servidor. Debe ser igual o posterior a periodoDesde.", example = "2025-01-31")
            @RequestParam(value = "periodoHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta){

        return ResponseEntity.ok(analyticsService.generarTablero(desde, hasta));
    }


}

