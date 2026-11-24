package ar.edu.uade.api.deliverygo.repository;

import ar.edu.uade.api.deliverygo.model.Resena;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ENT-01: acceso a la tabla resenas.
 * RN-05: ReviewService no usa delete ni update, aunque JpaRepository los hereda.
 * RN-02: existsBy... es el control previo; la garantía final es la restricción uk_resena_usuario_pedido_tipo.
 */
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    // REP-01 (CU-01, CU-02, RN-02): verifica que no exista ya una reseña del usuario para ese pedido y tipo
    boolean existsByUsuarioIdAndPedidoIdAndTipoObjetivo(Long usuarioId, Long pedidoId, TipoObjetivo tipoObjetivo);
    // REP-02 (CU-01, CU-02): persistir la reseña es el save() heredado de JpaRepository
    // REP-03 (CU-03, CU-04): reseñas de un comercio o repartidor
    List<Resena> findByObjetivoIdAndTipoObjetivo(Long objetivoId, TipoObjetivo tipoObjetivo);
    // REP-09 (CU-05, RN-03): reseñas creadas en el período, para el promedio y la cantidad del tablero.
    // Desde es inclusivo y hasta es exclusivo
    List<Resena> findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(
            LocalDateTime desde, LocalDateTime hasta);
}
