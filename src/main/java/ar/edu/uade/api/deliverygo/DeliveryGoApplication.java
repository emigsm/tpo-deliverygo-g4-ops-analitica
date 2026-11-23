package ar.edu.uade.api.deliverygo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Punto de entrada de la aplicación DeliveryGo - Operaciones y Analítica.
 * <p>
 * {@code @SpringBootApplication} combina {@code @Configuration}, {@code @EnableAutoConfiguration}
 * y {@code @ComponentScan} sobre el paquete {@code ar.edu.uade.api.deliverygo}. Por eso los
 * subpaquetes (controller, services, client, config, repository, mapper, excepcion, dto, model)
 * se escanean sin registro explícito, y Spring Data detecta las interfaces de {@code repository}.
 * <p>
 * Ver: {@code docs/arquitectura.md} y la teoría de Spring Boot en la guía de la materia.
 */
@SpringBootApplication
public class DeliveryGoApplication {
  public static void main(String[] args) { SpringApplication.run(DeliveryGoApplication.class, args); }
}
