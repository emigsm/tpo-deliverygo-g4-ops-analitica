package ar.edu.uade.api.deliverygo.repository;

import ar.edu.uade.api.deliverygo.model.Resena;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a ENT-01 ({@code resenas}).
 * <p>
 * Esta interfaz no declara métodos propios de actualización ni de borrado. Sin embargo, hereda
 * {@code delete} y {@code save} de {@link JpaRepository}: la inmutabilidad (RN-05) depende de que
 * {@code ReviewService} no los use, y de que {@code updatable = false} en la entidad.
 * <p>
 * {@code existsBy...} (REP-01) es el control previo a la inserción (RN-02). La garantía final frente
 * a duplicados es la restricción {@code uk_resena_usuario_pedido_tipo}: dos requests simultáneos pueden
 * pasar el control previo, y la BD rechaza el segundo (inferencia, a verificar con una prueba de concurrencia).
 */
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    // REP-01: (RN-02) Verificar que no exista ya una reseña antes de crearla
    boolean existsByUsuarioIdAndPedidoIdAndTipoObjetivo(Long usuarioId, Long pedidoId, TipoObjetivo tipoObjetivo);

    // REP-02: Persistir la reseña validada (generada por default)

    // REP-03: Reseñas de un comercio o repartidor
    List<Resena> findByObjetivoIdAndTipoObjetivo(Long objetivoId, TipoObjetivo tipoObjetivo);

    // Consulta por rango de fechas de creación. No tiene identificador REP en el código.
    List<Resena> findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(
            LocalDateTime desde, LocalDateTime hasta);
}
