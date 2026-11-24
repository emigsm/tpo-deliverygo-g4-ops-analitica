package ar.edu.uade.api.deliverygo.config;

import ar.edu.uade.api.deliverygo.dto.ErrorResponseDTO;
import ar.edu.uade.api.deliverygo.excepcion.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * DM-19: responde los errores de seguridad con ErrorResponseDTO.
 * GlobalExceptionHandler no los ve: Spring Security los resuelve antes de llegar al controller.
 * 401 UNAUTHENTICATED: falta la credencial, es inválida o venció. 403 FORBIDDEN: el rol no alcanza.
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        escribir(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED",
                "Falta la credencial (token o X-Api-Key), es inválida o está vencida");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escribir(request, response, HttpStatus.FORBIDDEN, "FORBIDDEN",
                "El rol del usuario no tiene permiso para esta operación");
    }

    private void escribir(HttpServletRequest request, HttpServletResponse response,
                          HttpStatus status, String code, String message) throws IOException {
        ErrorResponseDTO error = new ErrorResponseDTO(Instant.now(), status.value(), code, message,
                request.getRequestURI(), GlobalExceptionHandler.correlationId(request));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
