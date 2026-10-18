package ar.edu.uade.api.deliverygo.repository;

import ar.edu.uade.api.deliverygo.model.Resena;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    // REP-01: (RN-02) Verificar que no exista ya una reseña antes de crearla
    boolean existsByUsuarioIdAndPedidoIdAndTipoObjetivo(Long usuarioId, Long pedidoId, TipoObjetivo tipoObjetivo);

    // REP-02: Persistir la reseña validada (generada por default)

    // REP-03: Reseñas de un comercio o repartidor
    List<Resena> findByObjetivoIdAndTipoObjetivo(Long objetivoId, TipoObjetivo tipoObjetivo);
}