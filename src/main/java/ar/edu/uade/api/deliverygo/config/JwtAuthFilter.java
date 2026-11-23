package ar.edu.uade.api.deliverygo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * S1 - Filtro que identifica al usuario a partir del JWT.
 *
 * Un filtro es un "guardia en la puerta": se ejecuta ANTES de que la petición llegue al controller.
 *
 * Qué hace, paso a paso:
 * <ol>
 *   <li>Busca el header {@code Authorization: Bearer <token>}.</li>
 *   <li>Si no está, deja pasar la petición sin identidad. Más adelante Spring Security responderá 401 si la ruta exige login.</li>
 *   <li>Si está, valida el token con {@link JwtUtil} (firma y vencimiento).</li>
 *   <li>Si es válido, guarda en el contexto de seguridad: quién es ({@code sub}) y su rol ({@code role}).</li>
 *   <li>Si es inválido (vencido, firma falsa, mal formado), NO guarda nada. El resultado final es 401.</li>
 * </ol>
 *
 * Nunca decide si el usuario puede entrar a una ruta: eso lo hacen las reglas de {@link SecurityConfig}.
 * Este filtro solo responde "¿quién es?".
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Solo intentamos autenticar si el header existe y empieza con "Bearer ".
        if (header != null && header.startsWith(PREFIJO_BEARER)) {
            String token = header.substring(PREFIJO_BEARER.length()).trim();
            try {
                Claims datos = jwtUtil.validar(token);

                // Spring Security espera los roles con prefijo ROLE_ (ej: ROLE_CLIENTE).
                // Usuarios envía el rol sin prefijo, así que lo agregamos acá.
                String rol = datos.get("role", String.class);
                List<SimpleGrantedAuthority> permisos = List.of(new SimpleGrantedAuthority("ROLE_" + rol));

                // principal = id del usuario (claim sub). credentials = null porque ya no hay contraseña.
                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(datos.getSubject(), null, permisos);
                SecurityContextHolder.getContext().setAuthentication(autenticacion);

            } catch (JwtException | IllegalArgumentException e) {
                // Token inválido: dejamos el contexto vacío. Spring Security responderá 401.
                SecurityContextHolder.clearContext();
            }
        }

        // En todos los casos, la petición sigue su camino hacia el siguiente filtro / controller.
        chain.doFilter(request, response);
    }
}
