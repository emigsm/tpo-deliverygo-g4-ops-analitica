package ar.edu.uade.api.deliverygo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * S1 - Herramienta para trabajar con el JWT que emite el módulo Usuarios.
 *
 * Qué es un JWT (explicado simple):
 * <ul>
 *   <li>Es un texto con 3 partes separadas por puntos: {@code encabezado.datos.firma}.</li>
 *   <li>Los "datos" (claims) dicen quién es el usuario: {@code sub} = id del usuario, {@code role} = su rol
 *       (CLIENTE, COMERCIO, REPARTIDOR o ADMIN) y {@code exp} = cuándo vence.</li>
 *   <li>La "firma" se calcula con una clave secreta que conocen solo Usuarios y este módulo. Si alguien cambia los
 *       datos (por ejemplo, el rol), la firma deja de coincidir y el token se rechaza.</li>
 * </ul>
 *
 * Este módulo nunca crea tokens (eso lo hace Usuarios). Solo comprueba que sean auténticos y que no estén vencidos.
 * La clave viene de {@code jwt.secret} en application.properties (variable de entorno JWT_SECRET).
 *
 * El método {@link #generarToken} existe SOLO para que los tests puedan fabricar tokens de prueba.
 */
@Component
public class JwtUtil {

    /** Clave secreta ya convertida al formato que pide la librería jjwt. */
    private final SecretKey clave;

    public JwtUtil(@Value("${jwt.secret}") String secreto) {
        // HS256 necesita una clave de al menos 32 bytes. Si es más corta, Keys lanza una excepción al arrancar.
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Valida un token y devuelve sus datos (claims).
     *
     * Verifica dos cosas a la vez: que la firma sea correcta y que el token no esté vencido.
     *
     * @param token el token SIN el prefijo "Bearer "
     * @return los datos del token
     * @throws JwtException si el token está vencido, mal formado o tiene la firma inválida
     */
    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(clave)          // comprueba la firma con nuestra clave
                .build()
                .parseSignedClaims(token)   // también falla si exp ya pasó o si el token no está firmado
                .getPayload();
    }

    /**
     * Fabrica un token firmado. Solo se usa en tests (este módulo no emite tokens en producción).
     *
     * @param usuarioId      se guarda en el claim {@code sub} (como texto, igual que hace Usuarios)
     * @param rol            se guarda en el claim {@code role}, sin prefijo ROLE_
     * @param minutosDeVida  minutos hasta el vencimiento. Un valor negativo genera un token ya vencido
     */
    public String generarToken(Long usuarioId, String rol, long minutosDeVida) {
        Date ahora = new Date();
        Date vencimiento = new Date(ahora.getTime() + minutosDeVida * 60_000);
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("role", rol)
                .issuedAt(ahora)
                .expiration(vencimiento)
                .signWith(clave)
                .compact();
    }
}
