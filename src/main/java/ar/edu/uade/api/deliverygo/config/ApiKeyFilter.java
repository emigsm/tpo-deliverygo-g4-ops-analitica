package ar.edu.uade.api.deliverygo.config;
import ar.edu.uade.api.deliverygo.model.ModuloOrigen;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

/**
 * API-05 (CU-06): identifica al módulo emisor del evento con el header X-Api-Key (el x-api-key sirve para identificar a otros módulos)
 * Solo se ejecuta en POST /api/v1/analytics/events. Si la clave coincide con la de un módulo, guarda el nombre del módulo (PEDIDOS, PAGOS o LOGISTICA) con el rol MODULO. Si no coincide no guarda nada y responde 401.
 */
public class ApiKeyFilter extends OncePerRequestFilter {
    static final String RUTA_EVENTOS = "/api/v1/analytics/events";
    private final Map<ModuloOrigen, String> claves;

    public ApiKeyFilter(Map<ModuloOrigen, String> claves) {
        this.claves = claves;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod()) && RUTA_EVENTOS.equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String recibida = request.getHeader("X-Api-Key");

        if (recibida != null) {
            for (Map.Entry<ModuloOrigen, String> modulo : claves.entrySet()) {
                if (coincide(modulo.getValue(), recibida)) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(modulo.getKey().name(), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_MODULO"))));
                    break;
                }
            }
        }

        chain.doFilter(request, response);
    }

    // isEqual tarda lo mismo aunque la clave sea parecida, para no dar pistas con el tiempo de respuesta
    private boolean coincide(String esperada, String recibida) {

        if (esperada == null || esperada.isBlank()) return false;

        return MessageDigest.isEqual(esperada.getBytes(StandardCharsets.UTF_8),
                recibida.getBytes(StandardCharsets.UTF_8));
    }
}
