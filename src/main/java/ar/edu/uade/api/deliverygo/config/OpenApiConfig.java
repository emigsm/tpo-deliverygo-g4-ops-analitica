package ar.edu.uade.api.deliverygo.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import ar.edu.uade.api.deliverygo.model.FuenteMetrica;
import ar.edu.uade.api.deliverygo.model.TipoMetrica;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Documento OpenAPI que expone Springdoc (Swagger UI y /v3/api-docs).
 * Tiene dos beans:
 * - deliveryGoOpenAPI: título, versión, descripción general y esquema de seguridad JWT Bearer.
 * - implementedExamples: un OpenApiCustomizer que agrega a cada operación el header X-Correlation-Id, la respuesta 401, 403 en reviews y ejemplos JSON.
 * DM-19: JWT Bearer. El código de SecurityConfig es la fuente.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI deliveryGoOpenAPI() {
        return new OpenAPI().info(new Info().title("DeliveryGo API - Opiniones y Analítica")
                .version("1.1").description("API-01 a API-06 implementadas. "
                        + "Autenticación: JWT Bearer emitido por el módulo Usuarios (HS256). El id del usuario sale del claim sub. "
                        + "Roles: CLIENTE crea reseñas; ADMIN consulta tablero y métricas. Los eventos (API-05) los envían los módulos con X-Api-Key. "
                        + "Integraciones: mocks por defecto; el perfil integracion usa clientes HTTP para Usuarios y Comercios. "
                        + "Los errores usan code, message, timestamp UTC y correlationId, incluidos los de seguridad (401 y 403). "
                        + "Los errores del framework, como un JSON ilegible, conservan su formato."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        // API-05: los módulos emisores usan clave en lugar de JWT
                        .addSecuritySchemes("apiKeyAuth",
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-Api-Key")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * DOC: headers, respuestas comunes y ejemplos de cada operación.
     */
    @Bean
    public OpenApiCustomizer implementedExamples() {
        return api -> {
            api.getPaths().forEach((path, item) -> item.readOperations().forEach(operation -> {
                operation.addParametersItem(new io.swagger.v3.oas.models.parameters.HeaderParameter()
                        .name("X-Correlation-Id").required(false)
                        .description("UUID canónico opcional. Solo errores manejados lo reutilizan; ausente o inválido genera UUID.")
                        .schema(new io.swagger.v3.oas.models.media.StringSchema()));
                operation.getResponses().addApiResponse("401", new io.swagger.v3.oas.models.responses.ApiResponse()
                        .description("UNAUTHENTICATED: falta el token JWT, está vencido o su firma es inválida (ErrorResponseDTO)."));
                if (path.endsWith("/reviews")) {
                    operation.getResponses().addApiResponse("403", new io.swagger.v3.oas.models.responses.ApiResponse()
                            .description("FORBIDDEN: el token es válido pero su rol no es CLIENTE (ErrorResponseDTO)."));
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
                        json.addExamples("conMetricas", example("["
                                + metricaJson(FuenteMetrica.RESENAS, TipoMetrica.CANTIDAD_RESENAS, "2026-11-01", "2026-11-09", "3") + "]"));
                        json.addExamples("sinSnapshots", example("[]"));
                    } else if (status.equals("200") && path.endsWith("/dashboard")) {
                        // Los ejemplos se arman desde TipoMetrica, así origen y formula coinciden siempre con la respuesta real
                        String metricas = String.join(",",
                                metricaJson(FuenteMetrica.RESENAS, TipoMetrica.CANTIDAD_RESENAS, "2026-10-01", "2026-10-30", "2"),
                                metricaJson(FuenteMetrica.RESENAS, TipoMetrica.PROMEDIO_CALIFICACION, "2026-10-01", "2026-10-30", "4.50"),
                                metricaJson(FuenteMetrica.PEDIDOS, TipoMetrica.PEDIDOS_ENTREGADOS, "2026-10-01", "2026-10-30", "5"),
                                metricaJson(FuenteMetrica.PEDIDOS, TipoMetrica.CANTIDAD_PEDIDOS_CANCELADOS, "2026-10-01", "2026-10-30", "1"),
                                metricaJson(FuenteMetrica.PAGOS, TipoMetrica.CANTIDAD_PAGOS_APROBADOS, "2026-10-01", "2026-10-30", "4"),
                                metricaJson(FuenteMetrica.PAGOS, TipoMetrica.CANTIDAD_PAGOS_RECHAZADOS, "2026-10-01", "2026-10-30", "1"),
                                metricaJson(FuenteMetrica.LOGISTICA, TipoMetrica.CANTIDAD_ENTREGAS, "2026-10-01", "2026-10-30", "5"),
                                metricaJson(FuenteMetrica.LOGISTICA, TipoMetrica.TIEMPO_PROMEDIO_ENTREGA, "2026-10-01", "2026-10-30", "32.50"));
                        json.addExamples("completo", example("{\"generadoEn\":\"2026-10-30T12:00:00\",\"metricas\":[" + metricas + "]}"));
                    } else {
                        String code = switch (status) {
                            case "403" -> "FORBIDDEN";
                            case "409" -> "DUPLICATE_REVIEW";
                            case "422" -> "BUSINESS_RULE_VIOLATION";
                            case "503" -> "EXTERNAL_SERVICE_UNAVAILABLE";
                            default -> (path.endsWith("dashboard") || path.endsWith("/metrics")) ? "INVALID_PERIOD" : "INVALID_RATING";
                        };

                        String message = switch (code) {
                            case "FORBIDDEN" -> "El rol del usuario no tiene permiso para esta operación";
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

    // Arma el JSON de una métrica con su origen y fórmula tomados de TipoMetrica
    private static String metricaJson(FuenteMetrica fuente, TipoMetrica tipo, String desde, String hasta, String valor) {
        return "{\"fuente\":\"" + fuente + "\",\"tipoMetrica\":\"" + tipo + "\",\"origen\":\"" + tipo.getOrigen()
                + "\",\"formula\":\"" + tipo.getFormula() + "\",\"periodoDesde\":\"" + desde
                + "\",\"periodoHasta\":\"" + hasta + "\",\"valor\":" + valor + "}";
    }

    private static Example example(String json) {
        try {
            return new Example().value(io.swagger.v3.core.util.Json.mapper().readValue(json, Object.class));
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("Invalid OpenAPI example", ex);
        }
    }
}
