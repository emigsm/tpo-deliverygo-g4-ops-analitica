package ar.edu.uade.api.deliverygo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security para la API.
 *
 * Decisiones:
 * <ul>
 *   <li>S1: la identidad sale del JWT que emite el módulo Usuarios ({@code Authorization: Bearer <token>}).
 *       {@link JwtAuthFilter} lee y valida el token. Ya no se usa HTTP Basic ni el header temporal X-Usuario-Id.</li>
 *   <li>Sin sesión ({@code STATELESS}) y sin CSRF. La API la consumen otros equipos con Postman o curl, no usa
 *       cookies de sesión, y CSRF protege justamente contra el envío automático de cookies.</li>
 *   <li>Solo {@code GET /api/v1/status} es público. El resto exige un token válido.</li>
 *   <li>Sin token, o con token inválido o vencido, la respuesta es 401.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    /**
     * Define la cadena de filtros de seguridad.
     *
     * Orden de ejecución de una petición: primero {@link JwtAuthFilter} (¿quién es?) y después las reglas de
     * {@code authorizeHttpRequests} (¿puede entrar a esta ruta?). Las reglas se evalúan de arriba hacia abajo.
     *
     * @param jwtUtil validador de tokens (Spring lo inyecta solo porque es un {@code @Component})
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Nuestro filtro corre antes del filtro de login estándar de Spring.
                .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                // Sin identidad válida Spring respondería 403 por defecto. Lo correcto es 401 (no autenticado).
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/status").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
