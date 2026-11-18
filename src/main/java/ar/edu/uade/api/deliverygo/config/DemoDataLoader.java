package ar.edu.uade.api.deliverygo.config;

import ar.edu.uade.api.deliverygo.model.Resena;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.repository.ResenaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// Reseñas demo para que otros equipos prueben GET .../rating sin crear datos antes.
// Solo con mocks: con el perfil "integracion" los datos son reales y no se precarga nada.
// Acoplado a ComercioClientMock (comercio 10) y LogisticaClientMock (repartidor 20).
// Usuario 101 y pedidos 901/902 no existen en PedidosClientMock: no colisionan con RN-02
// al crear reseñas de los pedidos mock 1 y 2.
// TODO: centralizar los ids mock en constantes compartidas si crecen los datos de prueba.
@Configuration
@Profile("!integracion")
public class DemoDataLoader {

    @Bean
    public CommandLineRunner cargarResenasDemo(ResenaRepository resenaRepository) {
        return args -> {
            resenaRepository.save(new Resena(101L, 901L, TipoObjetivo.COMERCIO, 10L, 4, "Reseña demo precargada"));
            resenaRepository.save(new Resena(101L, 902L, TipoObjetivo.REPARTIDOR, 20L, 5, "Reseña demo precargada"));
        };
    }
}
