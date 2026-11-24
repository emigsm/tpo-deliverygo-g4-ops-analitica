package ar.edu.uade.api.deliverygo.config;
import ar.edu.uade.api.deliverygo.model.ModuloOrigen;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Map;

/**
 * DM-19: seguridad de la API con JWT.
 * Solo el GET /api/v1/status es público.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil,
                                                   SecurityErrorHandler errorHandler,
                                                   @Value("${eventos.api-key.pedidos}") String clavePedidos,
                                                   @Value("${eventos.api-key.pagos}") String clavePagos,
                                                   @Value("${eventos.api-key.logistica}") String claveLogistica) throws Exception {
        // API-05: una clave por módulo emisor
        Map<ModuloOrigen, String> clavesEventos = Map.of(
                ModuloOrigen.PEDIDOS, clavePedidos,
                ModuloOrigen.PAGOS, clavePagos,
                ModuloOrigen.LOGISTICA, claveLogistica);

        http
                .csrf(csrf -> csrf.disable())// SIN COOKIES
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new ApiKeyFilter(clavesEventos), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex// DM-19: los 401 y 403 usan ErrorResponseDTO, igual que el resto de la API
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/status").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews").hasRole("CLIENTE") // API-01 (CU-01, CU-02): solo CLIENTE
                        .requestMatchers(HttpMethod.GET, "/api/v1/analytics/dashboard", "/api/v1/analytics/metrics").hasRole("ADMIN")// API-04 (CU-05) y API-06 (CU-07): solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/analytics/events").hasRole("MODULO")// API-05 (CU-06): solo los módulos emisores, con X-Api-Key (no sirve un JWT)
                        .anyRequest().authenticated());// API-02 y API-03: cualquier token válido
        return http.build();
    }
}
