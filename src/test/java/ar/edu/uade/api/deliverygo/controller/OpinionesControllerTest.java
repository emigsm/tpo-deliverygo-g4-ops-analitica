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
 * API-01, API-02 (CU-01 a CU-03) y DM-19: seguridad y reglas de reseñas de punta a punta.
 * Usa los mocks de Usuarios, Comercios, Logística y Pedidos: pedido 1 (usuario 100, comercio 10, ENTREGADO),
 * pedido 2 (usuario 100, repartidor 20, ENTREGADO) y pedido 3 (usuario 100, comercio 10, EN_CAMINO).
 * Cada test que crea una reseña usa un pedido distinto, porque la base H2 se comparte entre tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpinionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String bearer(Long usuarioId, String rol) {
        return "Bearer " + jwtUtil.generarToken(usuarioId, rol, 60);
    }

    private String cuerpoResena(long pedidoId, String tipoObjetivo) {
        return "{\"pedidoId\":" + pedidoId + ",\"tipoObjetivo\":\"" + tipoObjetivo
                + "\",\"puntuacion\":5,\"comentario\":\"Buena atención\"}";
    }

    // ---------- Seguridad (DM-19) ----------

    @Test
    void crearResena_sinToken_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(1, "COMERCIO")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void crearResena_conTokenVencido_devuelve401() throws Exception {
        String vencido = "Bearer " + jwtUtil.generarToken(100L, "CLIENTE", -5);

        mockMvc.perform(post("/api/v1/reviews").header("Authorization", vencido)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(1, "COMERCIO")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void crearResena_conTextoQueNoEsToken_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(1, "COMERCIO")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearResena_conRolAdmin_devuelve403() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(1, "COMERCIO")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ---------- Reglas de reseñas ----------

    // CU-02: el usuarioId de la reseña sale del token, no del body
    @Test
    void crearResena_conTokenCliente_devuelve201ConUsuarioDelToken() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").header("Authorization", bearer(100L, "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(2, "REPARTIDOR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(100))
                .andExpect(jsonPath("$.objetivoId").value(20));
    }

    // RN-02: una reseña por usuario, pedido y tipo de objetivo
    @Test
    void crearResena_repetida_devuelve409() throws Exception {
        String token = bearer(100L, "CLIENTE");

        mockMvc.perform(post("/api/v1/reviews").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(1, "COMERCIO")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/reviews").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(1, "COMERCIO")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_REVIEW"));
    }

    // RN-01: solo se califica un pedido ENTREGADO
    @Test
    void crearResena_pedidoNoEntregado_devuelve422() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").header("Authorization", bearer(100L, "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(3, "COMERCIO")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    // RN-01: un pedido inexistente se trata como no entregado
    @Test
    void crearResena_pedidoInexistente_devuelve422() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").header("Authorization", bearer(100L, "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(999, "COMERCIO")))
                .andExpect(status().isUnprocessableEntity());
    }

    // RN-07: el pedido debe pertenecer al usuario del token (el pedido 1 es del usuario 100)
    @Test
    void crearResena_pedidoDeOtroUsuario_devuelve422() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").header("Authorization", bearer(999L, "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoResena(1, "COMERCIO")))
                .andExpect(status().isUnprocessableEntity());
    }

    // ---------- Rating (API-02) ----------

    @Test
    void ratingComercio_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/merchants/10/rating"))
                .andExpect(status().isUnauthorized());
    }

    // CU-03: cualquier token válido puede consultar el rating, y un comercio sin reseñas responde 200
    @Test
    void ratingComercio_conCualquierToken_devuelve200() throws Exception {
        mockMvc.perform(get("/api/v1/merchants/777/rating").header("Authorization", bearer(5L, "COMERCIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadResenas").value(0));
    }
}
