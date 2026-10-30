package ar.edu.uade.api.deliverygo.controller;

import ar.edu.uade.api.deliverygo.dto.DashboardResponse;
import ar.edu.uade.api.deliverygo.dto.RatingResponse;
import ar.edu.uade.api.deliverygo.dto.ResenaRequest;
import ar.edu.uade.api.deliverygo.dto.ResenaResponse;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.services.AnalyticsService;
import ar.edu.uade.api.deliverygo.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Crear reseña", description = "Registra una reseña sobre un comercio o un repartidor de un pedido entregado. " + "El usuario autenticado se recibe por header temporal X-Usuario-Id hasta que este armado SecurityConfig")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Resena creada"), @ApiResponse(responseCode = "400", description = "Puntuacion invalida (debe estar entre 1 y 5)"), @ApiResponse(responseCode = "409", description = "Ya existe una resena de ese usuario para ese pedido y tipo"), @ApiResponse(responseCode = "422", description = "Pedido no entregado, no pertenece al usuario, o usuario/comercio/repartidor invalido o inactivo"), @ApiResponse(responseCode = "503", description = "Un servicio externo (Pedidos, Usuarios, Comercio o Logistica) no esta disponible")})

    @PostMapping("/reviews")
    public ResponseEntity<ResenaResponse> crearResena(@RequestBody ResenaRequest resenaRequest, @Parameter(description = "Id del usuario autenticado (temporal, hasta tener JWT)", example = "100") @RequestHeader("X-Usuario-Id") Long usuarioIdAutenticado) {

        ResenaResponse response = reviewService.crearResena(resenaRequest, usuarioIdAutenticado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // API-02 (CU-03)
    @Operation(summary = "Calificación promedio de un comercio")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Calificación calculada"), @ApiResponse(responseCode = "503", description = "Servicio externo no disponible")})

    @GetMapping("/merchants/{id}/rating")
    public ResponseEntity<RatingResponse> obtenerRatingComercio(@Parameter(example = "10") @PathVariable("id") Long id) {

        return ResponseEntity.ok(reviewService.obtenerPromedio(TipoObjetivo.COMERCIO, id));

    }

    // API-03 (CU-04)
    @Operation(summary = "Calificación promedio de un repartidor")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Calificación calculada"), @ApiResponse(responseCode = "503", description = "Servicio externo no disponible")})
    @GetMapping("/couriers/{id}/rating")
    public ResponseEntity<RatingResponse> obtenerRatingRepartidor(@Parameter(example = "20") @PathVariable("id") Long id) {

        return ResponseEntity.ok(reviewService.obtenerPromedio(TipoObjetivo.REPARTIDOR, id));

    }

    @GetMapping("/analytics/dashboard")
    public ResponseEntity<DashboardResponse> obtenerTablero(
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta){

        return ResponseEntity.ok(analyticsService.generarTablero(desde, hasta));
    }

}
