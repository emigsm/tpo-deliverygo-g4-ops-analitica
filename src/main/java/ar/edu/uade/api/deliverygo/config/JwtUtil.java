package ar.edu.uade.api.deliverygo.config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * DM-19: valida los JWT que emite el módulo Usuarios (HS256, clave compartida en jwt.secret).
 * Este módulo no emite tokens.
 */
@Component
public class JwtUtil {
    private final SecretKey clave;

    public JwtUtil(@Value("${jwt.secret}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));// HS256 exige al menos 32 bytes
    }

    /** Devuelve los claims si la firma es correcta y el token no venció. Si no, lanza JwtException. */
    public Claims validar(String token) {
        return Jwts.parser()//crea el analizador del JWT
                .verifyWith(clave)//le paso la clave para validar la firma
                .build()
                .parseSignedClaims(token)//analiza el token, verifica su firma y vencimiento
                .getPayload();//obtengo el contenido del token, sus claims (cada propiedad)
    }

    /** Solo para tests. Un valor negativo en minutosDeVida genera un token ya vencido. */
    public String generarToken(Long usuarioId, String rol, long minutosDeVida) {
        Date ahora = new Date();
        Date vencimiento = new Date(ahora.getTime() + minutosDeVida * 60_000);
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))//identifico al usuario
                .claim("role", rol)//le agrego el rol
                .issuedAt(ahora)
                .expiration(vencimiento)
                .signWith(clave)
                .compact();//genero el texto final, une las partes del JWT y devuelve el token como un String (header + payload + firma)
    }
}
