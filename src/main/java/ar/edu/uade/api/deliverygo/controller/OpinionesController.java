package ar.edu.uade.api.deliverygo.controller;

import ar.edu.uade.api.deliverygo.dto.ErrorResponseDTO;
import ar.edu.uade.api.deliverygo.dto.RatingResponse;
import ar.edu.uade.api.deliverygo.dto.ResenaRequest;
import ar.edu.uade.api.deliverygo.dto.ResenaResponse;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
@Tag(name = "Opiniones", description = "Creación de reseñas y consulta de rating promedio")
public class OpinionesController {

    // Documentation-only schema: Spring Boot /error, not a controller response DTO.
    @Schema(name = "SpringBootError", description = "Respuesta JSON de Spring Boot para errores de binding; no es ErrorResponseDTO",
            requiredProperties = {"timestamp", "status", "error", "path"})
    public record SpringBootErrorSchema(java.time.Instant timestamp, int status, String error, String path) {}


private final ReviewService reviewService;

public OpinionesController(ReviewService reviewService) {
    this.reviewService = reviewService;
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResenaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Puntuación nula o fuera del rango 1 a 5 (ErrorResponseDTO); body/header inválido (Spring Boot)",
                    content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {ErrorResponseDTO.class, SpringBootErrorSchema.class}))),
            @ApiResponse(responseCode = "409", description = "Ya existe una reseña del usuario para ese pedido y tipo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "El pedido no existe o no fue entregado, no pertenece al usuario, o el usuario/objetivo no existe o está inactivo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "Un servicio externo requerido no está disponible",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PostMapping("/reviews")
    public ResponseEntity<ResenaResponse> crearResena(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la reseña. El DTO no declara restricciones Bean Validation.",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResenaRequest.class)))
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RatingResponse.class)))})

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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RatingResponse.class)))})
    @GetMapping("/couriers/{id}/rating")
    public ResponseEntity<RatingResponse> obtenerRatingRepartidor(
            @Parameter(description = "Id del repartidor", required = true, example = "20")
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(reviewService.obtenerPromedio(TipoObjetivo.REPARTIDOR, id));

    }

}
