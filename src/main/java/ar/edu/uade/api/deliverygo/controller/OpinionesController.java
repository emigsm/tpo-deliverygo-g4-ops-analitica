package ar.edu.uade.api.deliverygo.controller;

import ar.edu.uade.api.deliverygo.dto.DashboardResponse;
import ar.edu.uade.api.deliverygo.dto.RatingResponse;
import ar.edu.uade.api.deliverygo.dto.ResenaRequest;
import ar.edu.uade.api.deliverygo.dto.ResenaResponse;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.services.AnalyticsService;
import ar.edu.uade.api.deliverygo.services.ReviewService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
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
    @PostMapping("/reviews")
    public ResponseEntity<ResenaResponse> crearResena(@RequestBody ResenaRequest resenaRequest, @RequestHeader("X-Usuario-Id") Long usuarioIdAutenticado) {

        ResenaResponse response = reviewService.crearResena(resenaRequest, usuarioIdAutenticado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // API-02 (CU-03)
    @GetMapping("/merchants/{id}/rating")
    public ResponseEntity<RatingResponse> obtenerRatingComercio(@PathVariable("id") Long id) {

        return ResponseEntity.ok(reviewService.obtenerPromedio(TipoObjetivo.COMERCIO, id));

    }

    // API-03 (CU-04)
    @GetMapping("/couriers/{id}/rating")
    public ResponseEntity<RatingResponse> obtenerRatingRepartidor(@PathVariable("id") Long id) {

        return ResponseEntity.ok(reviewService.obtenerPromedio(TipoObjetivo.REPARTIDOR, id));

    }

    @GetMapping("/analytics/dashboard")
    public ResponseEntity<DashboardResponse> obtenerTablero(
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta){

        return ResponseEntity.ok(analyticsService.generarTablero(desde, hasta));
    }

}
