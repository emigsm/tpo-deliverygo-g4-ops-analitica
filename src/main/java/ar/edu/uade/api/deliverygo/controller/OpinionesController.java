package ar.edu.uade.api.deliverygo.controller;

import ar.edu.uade.api.deliverygo.dto.RatingResponse;
import ar.edu.uade.api.deliverygo.dto.ResenaRequest;
import ar.edu.uade.api.deliverygo.dto.ResenaResponse;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.services.ReviewService;
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
@RequestMapping("/api/v1")
public class OpinionesController {

    private final ReviewService reviewService;

    public OpinionesController(ReviewService reviewService) {
        this.reviewService = reviewService;
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
}
