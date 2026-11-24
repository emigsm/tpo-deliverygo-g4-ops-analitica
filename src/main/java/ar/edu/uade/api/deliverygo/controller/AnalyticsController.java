package ar.edu.uade.api.deliverygo.controller;
import java.time.Instant;
import java.util.Map;
import ar.edu.uade.api.deliverygo.controller.OpinionesController.SpringBootErrorSchema;
import ar.edu.uade.api.deliverygo.dto.*;
import ar.edu.uade.api.deliverygo.excepcion.ModuloOrigenNoAutorizadoException;
import ar.edu.uade.api.deliverygo.services.AnalyticsService;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API-04, API-05 y API-06 (CU-05 a CU-10): tablero de indicadores, métricas por período y registro de eventos.
 * También expone GET /status, un health check para verificar que el módulo está activo.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // API-05 (CU-06, SRV-04, DTO-11 -> DTO-12): responde 201 con el evento registrado.
    // Lo invocan otros módulos, no el Portal: se autentican con X-Api-Key, no con JWT (DM-19)
    @Operation(summary = "Registrar evento analítico",
            description = "CU-06 y RN-09: persiste un evento operativo emitido por otro módulo " +
                    "(Pedidos, Pagos o Logística) como insumo de las métricas. tipoEvento, moduloOrigen, " +
                    "entidadId y fechaEvento son obligatorios; valor y detalle son opcionales. " +
                    "Se autentica con el header X-Api-Key del módulo emisor, y moduloOrigen debe ser ese mismo módulo. " +
                    "Idempotencia y zona horaria de fechaEvento están pendientes de acordar.")
    @SecurityRequirement(name = "apiKeyAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento registrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventoAnaliticoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Faltan campos obligatorios (ErrorResponseDTO); " +
                    "body ilegible, enum inexistente o fecha mal formada (respuesta estándar de Spring)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {ErrorResponseDTO.class, SpringBootErrorSchema.class}))),
            @ApiResponse(responseCode = "403", description = "La X-Api-Key es de otro módulo que el declarado en moduloOrigen, " +
                    "o se usó un JWT en lugar de la clave (ErrorResponseDTO)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/analytics/events")
    public ResponseEntity<EventoAnaliticoResponse> registrarEvento(
            @RequestBody EventoAnaliticoRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        // API-05: el módulo dueño de la clave (authentication.getName()) debe coincidir con el moduloOrigen del evento
        if (request.getModuloOrigen() != null && !request.getModuloOrigen().name().equals(authentication.getName())) {
            throw new ModuloOrigenNoAutorizadoException("La clave pertenece al módulo " + authentication.getName()
                    + " y el evento declara moduloOrigen " + request.getModuloOrigen());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(analyticsService.registrarEvento(request));
    }

    // API-06 (CU-07, SRV-05, DTO-10): solo lee la tabla metricas, así que responde aunque Pedidos esté caído
    @Operation(summary = "Consultar métricas por período",
            description = "CU-07: lee snapshots previamente calculados y guardados. " +
                    "Devuelve los períodos completamente contenidos entre ambas fechas, con límites inclusivos. " +
                    "No recalcula, no modifica datos ni llama a otros módulos. Sin resultados devuelve 200 con []. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de métricas guardadas, posiblemente vacía",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MetricaResumen.class)))),
            @ApiResponse(responseCode = "400", description = "Fechas obligatorias, con formato inválido o rango invertido. " +
                    "El rango invertido usa ErrorResponseDTO; los errores de parámetros usan la respuesta de Spring.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {ErrorResponseDTO.class, SpringBootErrorSchema.class}))),
            @ApiResponse(responseCode = "403", description = "El token es válido pero su rol no es ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/analytics/metrics")
    public ResponseEntity<List<MetricaResumen>> consultarMetricasPorPeriodo(
            @Parameter(description = "Inicio inclusivo obligatorio, yyyy-MM-dd", example = "2026-11-01", required = true)
            @RequestParam("periodoDesde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodoDesde,
            @Parameter(description = "Fin inclusivo obligatorio, igual o posterior al inicio, yyyy-MM-dd", example = "2026-11-30", required = true)
            @RequestParam("periodoHasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodoHasta) {
        return ResponseEntity.ok(analyticsService.consultarMetricasPorPeriodo(periodoDesde, periodoHasta));
    }

    // API-04 (CU-05, SRV-03, DTO-04): las fechas omitidas toman la fecha actual. La lógica está en AnalyticsService.generarTablero
    @Operation(summary = "Consultar tablero de analítica",
            description = "CU-05: genera el tablero del período con sus 8 indicadores: reseñas (cantidad y promedio), " +
                    "pedidos entregados y cancelados (CU-08), pagos aprobados y rechazados (CU-09) y entregas con su " +
                    "tiempo promedio (CU-10). Cada indicador informa su origen y su fórmula. Requiere rol ADMIN. " +
                    "Cada fecha omitida toma la fecha actual; ambos extremos son inclusivos. " +
                    "Sin reseñas o sin tiempos de entrega, el promedio es null y no se persiste. " +
                    "No consulta a otros módulos: usa las reseñas propias y los eventos recibidos (DM-21).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tablero generado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período invertido o fecha con formato inválido. " +
                    "El período invertido usa ErrorResponseDTO; el formato inválido usa la respuesta estándar de Spring.",
                    content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {ErrorResponseDTO.class, SpringBootErrorSchema.class}))),
            @ApiResponse(responseCode = "403", description = "El token es válido pero su rol no es ADMIN",
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

    // Health check público: no consulta la BD ni otros módulos
    @Operation(summary = "Health check del módulo",
            description = "Devuelve 200 con status UP si el módulo está levantado.")
    @ApiResponse(responseCode = "200", description = "Módulo operativo")
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "module", "deliverygo-ops-analitica",
                "timestamp", Instant.now().toString()));
    }

}

