package ar.edu.uade.api.deliverygo.controller;

import ar.edu.uade.api.deliverygo.dto.DashboardResponse;
import ar.edu.uade.api.deliverygo.dto.ErrorResponseDTO;
import ar.edu.uade.api.deliverygo.dto.RatingResponse;
import ar.edu.uade.api.deliverygo.dto.ResenaRequest;
import ar.edu.uade.api.deliverygo.dto.ResenaResponse;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.services.AnalyticsService;
import ar.edu.uade.api.deliverygo.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Opiniones", description = "Creación de reseñas y consulta de rating promedio")
public class OpinionesController {

    private final ReviewService reviewService;
    private final AnalyticsService analyticsService;

    public OpinionesController(ReviewService reviewService, AnalyticsService analyticsService) {
        this.reviewService = reviewService;
        this.analyticsService = analyticsService;
    }

    // API-01 (CU-01, CU-02)
    // TODO: reemplazar por el usuarioId que resuelva SecurityConfig
    // Temporalmente se recibe usuarioId por header X-Usuario-Id

    @Operation(
            summary = "Crear reseña",
            description = "CU-01 y CU-02: registra una reseña de comercio o repartidor para un pedido entregado. " +
                    "La identidad del usuario se recibe temporalmente en X-Usuario-Id; se reemplazará al integrar JWT. " +
                    "La puntuación se valida en el servicio y debe estar entre 1 y 5; los demás campos del DTO no " +
                    "tienen restricciones Bean Validation declaradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reseña creada",
                    content = @Content(schema = @Schema(implementation = ResenaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Puntuación nula o fuera del rango 1 a 5",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe una reseña del usuario para ese pedido y tipo",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "El pedido no existe o no fue entregado, no pertenece al usuario, o el usuario/objetivo no existe o está inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "Un servicio externo requerido no está disponible",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PostMapping("/reviews")
    public ResponseEntity<ResenaResponse> crearResena(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la reseña. El DTO no declara restricciones Bean Validation.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResenaRequest.class)))
            @RequestBody ResenaRequest resenaRequest,
            @Parameter(name = "X-Usuario-Id", in = ParameterIn.HEADER, required = true,
                    description = "Id del usuario que realiza la operación. Header temporal hasta integrar JWT.",
                    example = "100")
            @RequestHeader("X-Usuario-Id") Long usuarioIdAutenticado) {

        ResenaResponse response = reviewService.crearResena(resenaRequest, usuarioIdAutenticado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // API-02 (CU-03)
    @Operation(summary = "Calificación promedio de un comercio",
            description = "CU-03: consulta la calificación promedio y la cantidad de reseñas del comercio.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Calificación calculada. Si no hay reseñas, promedio es null y cantidadResenas es 0.",
            content = @Content(schema = @Schema(implementation = RatingResponse.class)))})

    @GetMapping("/merchants/{id}/rating")
    public ResponseEntity<RatingResponse> obtenerRatingComercio(
            @Parameter(description = "Id del comercio", required = true, example = "10")
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(reviewService.obtenerPromedio(TipoObjetivo.COMERCIO, id));

    }

    // API-03 (CU-04)
    @Operation(summary = "Calificación promedio de un repartidor",
            description = "CU-04: consulta la calificación promedio y la cantidad de reseñas del repartidor.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Calificación calculada. Si no hay reseñas, promedio es null y cantidadResenas es 0.",
            content = @Content(schema = @Schema(implementation = RatingResponse.class)))})
    @GetMapping("/couriers/{id}/rating")
    public ResponseEntity<RatingResponse> obtenerRatingRepartidor(
            @Parameter(description = "Id del repartidor", required = true, example = "20")
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(reviewService.obtenerPromedio(TipoObjetivo.REPARTIDOR, id));

    }

    @Operation(summary = "Consultar tablero de analítica",
            description = "CU-05, RN-03 y RN-06: obtiene cantidad de reseñas, promedio de calificación y pedidos entregados. " +
                    "Cada fecha omitida toma la fecha actual del servidor; ambos extremos son inclusivos. " +
                    "Sin reseñas, el promedio es null y no se persiste. INT-05 es un contrato propuesto pendiente " +
                    "de confirmación con Pedidos. Si falla esa fuente, usa exclusivamente el snapshot del período exacto " +
                    "y responde con parcial=true; sin respaldo válido responde 503.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tablero generado; parcial=true indica uso de un snapshot de Pedidos",
                    content = @Content(schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período invertido o fecha con formato inválido. " +
                    "El período invertido usa ErrorResponseDTO; el formato inválido usa la respuesta estándar de Spring."),
            @ApiResponse(responseCode = "503", description = "Pedidos no disponible o respuesta inválida, sin snapshot válido del período exacto",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
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
