package ar.edu.uade.api.deliverygo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI deliveryGoOpenAPI() {
        return new OpenAPI().info(new Info().title("DeliveryGo API - Opiniones y Analítica")
                .version("1.0").description("API-01 a API-04 y API-06 implementadas. API-06 consulta snapshots guardados. Integraciones externas mock. "
                        + "Spring Security autoconfigurado: HTTP Basic o sesión de formulario y CSRF en POST. "
                        + "X-Usuario-Id es identidad de negocio temporal, no autentica. JWT Bearer es futuro. "
                        + "API-05 sigue siendo una propuesta sin implementación. Los YAML presentados se conservan como referencia de la entrega. "
                        + "Cambio incompatible: errores manejados usan code/message en lugar de error/mensaje, "
                        + "timestamp UTC y correlationId. Errores del framework y seguridad conservan su formato."))
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }

    @Bean
    public OpenApiCustomizer implementedExamples() {
        return api -> {
            api.getPaths().forEach((path, item) -> item.readOperations().forEach(operation -> {
                operation.addParametersItem(new io.swagger.v3.oas.models.parameters.HeaderParameter()
                        .name("X-Correlation-Id").required(false)
                        .description("UUID canónico opcional. Solo errores manejados lo reutilizan; ausente o inválido genera UUID.")
                        .schema(new io.swagger.v3.oas.models.media.StringSchema()));
                operation.getResponses().addApiResponse("401", new io.swagger.v3.oas.models.responses.ApiResponse()
                        .description("Spring Security: autenticación requerida o credenciales inválidas (Accept: application/json). No garantiza ErrorResponseDTO."));
                if (path.endsWith("/reviews")) {
                    operation.getResponses().addApiResponse("403", new io.swagger.v3.oas.models.responses.ApiResponse()
                            .description("Spring Security: POST sin token CSRF válido, incluso con HTTP Basic. No garantiza ErrorResponseDTO."));
                }
                operation.getResponses().forEach((status, response) -> {
                    if (response.getContent() == null) return;
                    var json = response.getContent().get("application/json");
                    if (json == null) return;
                    if (status.equals("201")) {
                        json.addExamples("creada", example("""
                                {"id":1,"usuarioId":100,"pedidoId":1,"tipoObjetivo":"COMERCIO","objetivoId":10,"puntuacion":5,"comentario":"Buena atención","fechaCreacion":"2026-10-30T12:00:00"}
                                """));
                    } else if (status.equals("200") && path.endsWith("/rating")) {
                        boolean merchant = path.contains("merchants");
                        json.addExamples("conResenas", example("{\"objetivoId\":" + (merchant ? 10 : 20)
                                + ",\"tipoObjetivo\":\"" + (merchant ? "COMERCIO" : "REPARTIDOR")
                                + "\",\"promedio\":4.75,\"cantidadResenas\":4}"));
                        json.addExamples("sinResenas", example("{\"objetivoId\":" + (merchant ? 10 : 20)
                                + ",\"tipoObjetivo\":\"" + (merchant ? "COMERCIO" : "REPARTIDOR")
                                + "\",\"promedio\":null,\"cantidadResenas\":0}"));
                    } else if (status.equals("200") && path.endsWith("/metrics")) {
                        json.addExamples("conMetricas", example("""
                                [{"fuente":"RESENAS","tipoMetrica":"CANTIDAD_RESENAS","periodoDesde":"2026-11-01","periodoHasta":"2026-11-09","valor":3}]
                                """));
                        json.addExamples("sinSnapshots", example("[]"));
                    } else if (status.equals("200") && path.endsWith("/dashboard")) {
                        String dashboard = """
                                {"generadoEn":"2026-10-30T12:00:00","metricas":[{"fuente":"RESENAS","tipoMetrica":"CANTIDAD_RESENAS","periodoDesde":"2026-10-01","periodoHasta":"2026-10-30","valor":0},{"fuente":"RESENAS","tipoMetrica":"PROMEDIO_CALIFICACION","periodoDesde":"2026-10-01","periodoHasta":"2026-10-30","valor":null},{"fuente":"PEDIDOS","tipoMetrica":"PEDIDOS_ENTREGADOS","periodoDesde":"2026-10-01","periodoHasta":"2026-10-30","valor":2}],"parcial":false}
                                """;
                        json.addExamples("completo", example(dashboard));
                        json.addExamples("respaldoPeriodoExacto", example(dashboard.replace("false", "true")));
                    } else {
                        String code = switch (status) {
                            case "409" -> "DUPLICATE_REVIEW";
                            case "422" -> "BUSINESS_RULE_VIOLATION";
                            case "503" -> "EXTERNAL_SERVICE_UNAVAILABLE";
                            default -> (path.endsWith("dashboard") || path.endsWith("/metrics")) ? "INVALID_PERIOD" : "INVALID_RATING";
                        };
                        String message = switch (code) {
                            case "DUPLICATE_REVIEW" -> "Ya existe una reseña para ese pedido y tipo";
                            case "BUSINESS_RULE_VIOLATION" -> "El pedido no fue entregado";
                            case "EXTERNAL_SERVICE_UNAVAILABLE" -> "Servicio externo no disponible";
                            case "INVALID_PERIOD" -> path.endsWith("/metrics")
                                    ? "Ambas fechas son obligatorias y periodoDesde debe ser igual o anterior a periodoHasta"
                                    : "El periodo es invalido";
                            default -> "La puntuación debe estar entre 1 y 5";
                        };
                        json.addExamples("negocio", example("{\"timestamp\":\"2026-10-30T12:00:00Z\",\"status\":"
                                + status + ",\"code\":\"" + code + "\",\"message\":\"" + message
                                + "\",\"path\":\"" + path + "\",\"correlationId\":\"2aa05de5-76a4-46e3-a7a1-e4572cf602a6\"}"));
                        if (status.equals("400") && (path.endsWith("dashboard") || path.endsWith("/metrics"))) {
                            json.addExamples("fechaMalFormada", example("{\"timestamp\":\"2026-10-30T12:00:00Z\","
                                    + "\"status\":400,\"error\":\"Bad Request\",\"path\":\"" + path + "\"}"));
                        }
                    }
                });
            }));
        };
    }

    private static Example example(String json) {
        try {
            return new Example().value(io.swagger.v3.core.util.Json.mapper().readValue(json, Object.class));
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("Invalid OpenAPI example", ex);
        }
    }
}
