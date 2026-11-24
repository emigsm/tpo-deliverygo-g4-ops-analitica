package ar.edu.uade.api.deliverygo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// DM-19: el módulo solo acepta tokens con firma correcta y sin vencer
class JwtUtilTest {

    private static final String CLAVE = "clave-de-prueba-de-al-menos-32-caracteres";

    private final JwtUtil jwtUtil = new JwtUtil(CLAVE);

    @Test
    void tokenValido_devuelveIdYRol() {
        String token = jwtUtil.generarToken(100L, "CLIENTE", 60);

        Claims datos = jwtUtil.validar(token);

        assertEquals("100", datos.getSubject());
        assertEquals("CLIENTE", datos.get("role", String.class));
    }

    @Test
    void tokenVencido_lanzaExcepcion() {
        String vencido = jwtUtil.generarToken(100L, "CLIENTE", -5);

        assertThrows(JwtException.class, () -> jwtUtil.validar(vencido));
    }

    @Test
    void tokenFirmadoConOtraClave_lanzaExcepcion() {
        JwtUtil otroEmisor = new JwtUtil("otra-clave-distinta-de-al-menos-32-caracteres");
        String tokenFalso = otroEmisor.generarToken(1L, "ADMIN", 60);

        assertThrows(JwtException.class, () -> jwtUtil.validar(tokenFalso));
    }

    @Test
    void textoQueNoEsUnToken_lanzaExcepcion() {
        // Por ejemplo, mandar solo la palabra del rol en lugar de un JWT
        assertThrows(JwtException.class, () -> jwtUtil.validar("ADMIN"));
    }

    @Test
    void tokenConElPayloadAdulterado_lanzaExcepcion() {
        String token = jwtUtil.generarToken(100L, "CLIENTE", 60);
        String[] partes = token.split("\\.");
        // Se reemplaza el payload por otro (sub 1, role ADMIN) y se conserva la firma original
        String payloadFalso = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"1\",\"role\":\"ADMIN\"}".getBytes());
        String adulterado = partes[0] + "." + payloadFalso + "." + partes[2];

        assertThrows(JwtException.class, () -> jwtUtil.validar(adulterado));
    }
}
