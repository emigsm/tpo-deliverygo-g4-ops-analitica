package ar.edu.uade.api.deliverygo.controller;

import ar.edu.uade.api.deliverygo.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API-04, API-05, API-06 (CU-05 a CU-07) y DM-19: seguridad y validaciones del tablero, métricas y eventos.
 * Las claves X-Api-Key son los valores por defecto de application.properties (eventos.api-key.*).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerTest {

    private static final String CLAVE_PAGOS = "clave-demo-pagos-2026";
    private static final String CLAVE_PEDIDOS = "clave-demo-pedidos-2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String bearer(Long usuarioId, String rol) {
        return "Bearer " + jwtUtil.generarToken(usuarioId, rol, 60);
    }

    private String evento(long entidadId, String fechaEvento) {
        return "{\"tipoEvento\":\"PAGO_APROBADO\",\"moduloOrigen\":\"PAGOS\",\"entidadId\":" + entidadId
                + ",\"fechaEvento\":\"" + fechaEvento + "\"}";
    }

    // ---------- Disponibilidad ----------

    @Test
    void status_esPublico() throws Exception {
        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // ---------- API-04: tablero (solo ADMIN) ----------

    @Test
    void tablero_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tablero_conRolCliente_devuelve403() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard").header("Authorization", bearer(100L, "CLIENTE")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void tablero_conRolAdmin_devuelve200() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard").header("Authorization", bearer(1L, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas").isArray());
    }

    // CU-05: el período no puede tener desde posterior a hasta
    @Test
    void tablero_conPeriodoInvertido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard")
                        .param("periodoDesde", "2026-11-20").param("periodoHasta", "2026-11-01")
                        .header("Authorization", bearer(1L, "ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }

    // ---------- API-06: métricas (solo ADMIN) ----------

    @Test
    void metricas_conRolCliente_devuelve403() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/metrics")
                        .param("periodoDesde", "2026-11-01").param("periodoHasta", "2026-11-30")
                        .header("Authorization", bearer(100L, "CLIENTE")))
                .andExpect(status().isForbidden());
    }

    // CU-07: un período sin snapshots responde 200 con lista vacía
    @Test
    void metricas_periodoSinDatos_devuelveListaVacia() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/metrics")
                        .param("periodoDesde", "2020-01-01").param("periodoHasta", "2020-01-31")
                        .header("Authorization", bearer(1L, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // CU-07: las dos fechas son obligatorias
    @Test
    void metricas_sinFechas_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/metrics").header("Authorization", bearer(1L, "ADMIN")))
                .andExpect(status().isBadRequest());
    }

    // ---------- API-05: eventos (solo módulos con X-Api-Key) ----------

    @Test
    void evento_conClaveDelModulo_devuelve201() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events").header("X-Api-Key", CLAVE_PAGOS)
                        .contentType(MediaType.APPLICATION_JSON).content(evento(5, "2026-11-20T10:00:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void evento_sinClave_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events")
                        .contentType(MediaType.APPLICATION_JSON).content(evento(5, "2026-11-20T10:00:00")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void evento_conClaveInventada_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events").header("X-Api-Key", "cualquiera-123456789")
                        .contentType(MediaType.APPLICATION_JSON).content(evento(5, "2026-11-20T10:00:00")))
                .andExpect(status().isUnauthorized());
    }

    // API-05: la clave de Pedidos no puede reportar eventos de Pagos
    @Test
    void evento_conClaveDeOtroModulo_devuelve403() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events").header("X-Api-Key", CLAVE_PEDIDOS)
                        .contentType(MediaType.APPLICATION_JSON).content(evento(5, "2026-11-20T10:00:00")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MODULE_ORIGIN_MISMATCH"));
    }

    // API-05: un JWT, aunque sea ADMIN, no reemplaza a la clave de un módulo
    @Test
    void evento_conJwtAdmin_devuelve403() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(evento(5, "2026-11-20T10:00:00")))
                .andExpect(status().isForbidden());
    }

    // RN-09: entidadId positivo
    @Test
    void evento_conEntidadIdCero_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events").header("X-Api-Key", CLAVE_PAGOS)
                        .contentType(MediaType.APPLICATION_JSON).content(evento(0, "2026-11-20T10:00:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ANALYTICS_EVENT"));
    }

    // RN-09: fechaEvento no puede ser futura
    @Test
    void evento_conFechaFutura_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events").header("X-Api-Key", CLAVE_PAGOS)
                        .contentType(MediaType.APPLICATION_JSON).content(evento(5, "2099-01-01T10:00:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ANALYTICS_EVENT"));
    }

    // RN-09: faltan campos obligatorios
    @Test
    void evento_sinCamposObligatorios_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events").header("X-Api-Key", CLAVE_PAGOS)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"moduloOrigen\":\"PAGOS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ANALYTICS_EVENT"));
    }
}
