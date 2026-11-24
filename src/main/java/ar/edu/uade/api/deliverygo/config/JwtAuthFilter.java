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
 * DM-19: lee el header Authorization Bearer, valida el token y guarda el usuario (sub) y su rol.
 * Si no hay token o es inválido no guarda nada, y Spring Security responde 401.
 * Los permisos por ruta se definen en SecurityConfig.
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

        if (header != null && header.startsWith(PREFIJO_BEARER)) {
            String token = header.substring(PREFIJO_BEARER.length()).trim();
            try {
                Claims datos = jwtUtil.validar(token);
                Long.parseLong(datos.getSubject());// El sub tiene que ser un número; así los controllers pueden convertirlo sin riesgo

                // Usuarios manda el rol sin prefijo; Spring Security lo espera como ROLE_xxx
                List<SimpleGrantedAuthority> permisos = List.of(new SimpleGrantedAuthority("ROLE_" + datos.get("role", String.class)));

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(datos.getSubject(), null, permisos));

            } catch (JwtException | IllegalArgumentException e) { // incluye NumberFormatException
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
