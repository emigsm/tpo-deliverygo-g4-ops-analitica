package ar.edu.uade.api.deliverygo.services;

import ar.edu.uade.api.deliverygo.client.ComercioClient;
import ar.edu.uade.api.deliverygo.client.LogisticaClient;
import ar.edu.uade.api.deliverygo.client.PedidosClient;
import ar.edu.uade.api.deliverygo.client.UsuariosClient;
import ar.edu.uade.api.deliverygo.dto.ComercioDTO;
import ar.edu.uade.api.deliverygo.dto.PedidoDTO;
import ar.edu.uade.api.deliverygo.dto.RatingResponse;
import ar.edu.uade.api.deliverygo.dto.RepartidorDTO;
import ar.edu.uade.api.deliverygo.dto.ResenaRequest;
import ar.edu.uade.api.deliverygo.dto.ResenaResponse;
import ar.edu.uade.api.deliverygo.dto.UsuarioDTO;
import ar.edu.uade.api.deliverygo.excepcion.DuplicateReviewException;
import ar.edu.uade.api.deliverygo.excepcion.ExternalEntityInvalidException;
import ar.edu.uade.api.deliverygo.excepcion.ExternalServiceUnavailableException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidCommentException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidOrderIdException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidRatingException;
import ar.edu.uade.api.deliverygo.excepcion.InvalidTargetTypeException;
import ar.edu.uade.api.deliverygo.excepcion.OrderNotDeliveredException;
import ar.edu.uade.api.deliverygo.excepcion.OrderNotOwnedException;
import ar.edu.uade.api.deliverygo.model.Resena;
import ar.edu.uade.api.deliverygo.model.TipoObjetivo;
import ar.edu.uade.api.deliverygo.repository.ResenaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * SRV-01 y SRV-02 (CU-01 a CU-04): reglas de las reseñas, sin base de datos ni otros módulos.
 * Los repositorios y los clientes son mocks de Mockito: cada test define qué responden y verifica qué hizo el servicio.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

    private static final Long USUARIO = 100L;

    @Mock
    private ResenaRepository resenaRepository;
    @Mock
    private PedidosClient pedidosClient;
    @Mock
    private UsuariosClient usuariosClient;
    @Mock
    private ComercioClient comercioClient;
    @Mock
    private LogisticaClient logisticaClient;

    @InjectMocks
    private ReviewService reviewService;

    // ---------- Datos de prueba ----------

    private ResenaRequest request(Long pedidoId, TipoObjetivo tipo, Integer puntuacion, String comentario) {
        ResenaRequest request = new ResenaRequest();
        request.setPedidoId(pedidoId);
        request.setTipoObjetivo(tipo);
        request.setPuntuacion(puntuacion);
        request.setComentario(comentario);
        return request;
    }

    // Pedido 1 entregado del usuario 100, con comercio 10. El usuario y el comercio existen y están activos
    private void pedidoDeComercioValido() {
        when(pedidosClient.obtenerPedido(1L)).thenReturn(Optional.of(new PedidoDTO(1L, USUARIO, 10L, null, "ENTREGADO")));
        when(usuariosClient.obtenerUsuario(USUARIO)).thenReturn(Optional.of(new UsuarioDTO(USUARIO, true)));
        when(comercioClient.obtenerComercio(10L)).thenReturn(Optional.of(new ComercioDTO(10L, true)));
    }

    private Resena resena(int puntuacion) {
        return new Resena(USUARIO, 1L, TipoObjetivo.COMERCIO, 10L, puntuacion, null);
    }

    // ---------- CU-01, CU-02: casos exitosos ----------

    @Test
    void crearResenaDeComercio_guardaConUsuarioYObjetivoDelPedido() {
        pedidoDeComercioValido();

        ResenaResponse response = reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 5, "Buena atención"), USUARIO);

        ArgumentCaptor<Resena> guardada = ArgumentCaptor.forClass(Resena.class);
        verify(resenaRepository).save(guardada.capture());
        assertEquals(USUARIO, guardada.getValue().getUsuarioId());
        assertEquals(10L, guardada.getValue().getObjetivoId());
        assertEquals(5, guardada.getValue().getPuntuacion());
        assertEquals(10L, response.getObjetivoId());
    }

    @Test
    void crearResenaDeRepartidor_tomaElRepartidorDelPedido() {
        when(pedidosClient.obtenerPedido(2L)).thenReturn(Optional.of(new PedidoDTO(2L, USUARIO, null, 20L, "ENTREGADO")));
        when(usuariosClient.obtenerUsuario(USUARIO)).thenReturn(Optional.of(new UsuarioDTO(USUARIO, true)));
        when(logisticaClient.obtenerRepartidor(20L)).thenReturn(Optional.of(new RepartidorDTO(20L, true)));

        ResenaResponse response = reviewService.crearResena(request(2L, TipoObjetivo.REPARTIDOR, 4, null), USUARIO);

        assertEquals(20L, response.getObjetivoId());
        assertEquals(TipoObjetivo.REPARTIDOR, response.getTipoObjetivo());
    }

    // RN-04: los extremos 1 y 5 son válidos
    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    void crearResena_conPuntuacionEnLosExtremos_guarda(int puntuacion) {
        pedidoDeComercioValido();

        reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, puntuacion, null), USUARIO);

        verify(resenaRepository).save(any(Resena.class));
    }

    // ---------- RN-02: reseña duplicada ----------

    @Test
    void crearResena_duplicada_lanzaExcepcionYNoGuarda() {
        pedidoDeComercioValido();
        when(resenaRepository.existsByUsuarioIdAndPedidoIdAndTipoObjetivo(USUARIO, 1L, TipoObjetivo.COMERCIO)).thenReturn(true);

        assertThrows(DuplicateReviewException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 5, null), USUARIO));

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    // RN-02: el duplicado se controla antes que la puntuación
    @Test
    void crearResena_duplicadaConPuntuacionInvalida_respondeDuplicada() {
        pedidoDeComercioValido();
        when(resenaRepository.existsByUsuarioIdAndPedidoIdAndTipoObjetivo(USUARIO, 1L, TipoObjetivo.COMERCIO)).thenReturn(true);

        assertThrows(DuplicateReviewException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 9, null), USUARIO));
    }

    // ---------- RN-01: pedido no entregado ----------

    @Test
    void crearResena_pedidoEnCamino_lanzaOrderNotDelivered() {
        when(pedidosClient.obtenerPedido(3L)).thenReturn(Optional.of(new PedidoDTO(3L, USUARIO, 10L, null, "EN_CAMINO")));

        assertThrows(OrderNotDeliveredException.class,
                () -> reviewService.crearResena(request(3L, TipoObjetivo.COMERCIO, 5, null), USUARIO));

        verifyNoInteractions(usuariosClient, comercioClient);
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    void crearResena_pedidoInexistente_lanzaOrderNotDelivered() {
        when(pedidosClient.obtenerPedido(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotDeliveredException.class,
                () -> reviewService.crearResena(request(999L, TipoObjetivo.COMERCIO, 5, null), USUARIO));
    }

    // ---------- RN-07: el pedido debe ser del usuario ----------

    @Test
    void crearResena_pedidoDeOtroUsuario_lanzaOrderNotOwned() {
        pedidoDeComercioValido();

        assertThrows(OrderNotOwnedException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 5, null), 999L));

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    // RN-07: un pedido sin usuarioId no debe provocar NullPointerException
    @Test
    void crearResena_pedidoSinUsuario_lanzaOrderNotOwned() {
        when(pedidosClient.obtenerPedido(1L)).thenReturn(Optional.of(new PedidoDTO(1L, null, 10L, null, "ENTREGADO")));

        assertThrows(OrderNotOwnedException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 5, null), USUARIO));
    }

    // ---------- RN-08: usuario y objetivo existentes y activos ----------

    @Test
    void crearResena_usuarioInactivo_lanzaExternalEntityInvalid() {
        when(pedidosClient.obtenerPedido(1L)).thenReturn(Optional.of(new PedidoDTO(1L, USUARIO, 10L, null, "ENTREGADO")));
        when(usuariosClient.obtenerUsuario(USUARIO)).thenReturn(Optional.of(new UsuarioDTO(USUARIO, false)));

        assertThrows(ExternalEntityInvalidException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 5, null), USUARIO));
    }

    @Test
    void crearResena_comercioInexistente_lanzaExternalEntityInvalid() {
        when(pedidosClient.obtenerPedido(1L)).thenReturn(Optional.of(new PedidoDTO(1L, USUARIO, 10L, null, "ENTREGADO")));
        when(usuariosClient.obtenerUsuario(USUARIO)).thenReturn(Optional.of(new UsuarioDTO(USUARIO, true)));
        when(comercioClient.obtenerComercio(10L)).thenReturn(Optional.empty());

        assertThrows(ExternalEntityInvalidException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 5, null), USUARIO));
    }

    @Test
    void crearResena_repartidorInactivo_lanzaExternalEntityInvalid() {
        when(pedidosClient.obtenerPedido(2L)).thenReturn(Optional.of(new PedidoDTO(2L, USUARIO, null, 20L, "ENTREGADO")));
        when(usuariosClient.obtenerUsuario(USUARIO)).thenReturn(Optional.of(new UsuarioDTO(USUARIO, true)));
        when(logisticaClient.obtenerRepartidor(20L)).thenReturn(Optional.of(new RepartidorDTO(20L, false)));

        assertThrows(ExternalEntityInvalidException.class,
                () -> reviewService.crearResena(request(2L, TipoObjetivo.REPARTIDOR, 5, null), USUARIO));
    }

    // DM-13: el pedido 1 no tiene repartidor, así que no se puede calificar a un repartidor
    @Test
    void crearResenaDeRepartidor_pedidoSinRepartidor_lanzaExternalEntityInvalid() {
        pedidoDeComercioValido();

        assertThrows(ExternalEntityInvalidException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.REPARTIDOR, 5, null), USUARIO));

        verifyNoInteractions(logisticaClient);
    }

    // ---------- RN-04: puntuación de 1 a 5 ----------

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, 6, -1})
    void crearResena_conPuntuacionInvalida_lanzaInvalidRating(Integer puntuacion) {
        pedidoDeComercioValido();

        assertThrows(InvalidRatingException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, puntuacion, null), USUARIO));

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    // ---------- CU-01, CU-02: validaciones de entrada (no llaman a otros módulos) ----------

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -5})
    void crearResena_conPedidoIdInvalido_lanzaInvalidOrderId(Long pedidoId) {
        assertThrows(InvalidOrderIdException.class,
                () -> reviewService.crearResena(request(pedidoId, TipoObjetivo.COMERCIO, 5, null), USUARIO));

        verifyNoInteractions(pedidosClient);
    }

    @Test
    void crearResena_sinTipoObjetivo_lanzaInvalidTargetType() {
        assertThrows(InvalidTargetTypeException.class,
                () -> reviewService.crearResena(request(1L, null, 5, null), USUARIO));

        verifyNoInteractions(pedidosClient);
    }

    @Test
    void crearResena_conComentarioDeMasDe1000Caracteres_lanzaInvalidComment() {
        String comentarioLargo = "x".repeat(1001);

        assertThrows(InvalidCommentException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 5, comentarioLargo), USUARIO));

        verifyNoInteractions(pedidosClient);
    }

    // ---------- Error de comunicación con otro módulo ----------

    // INT-01: si Pedidos no responde, el error se propaga (503) y no se guarda nada
    @Test
    void crearResena_conPedidosCaido_propagaElErrorYNoGuarda() {
        when(pedidosClient.obtenerPedido(1L)).thenThrow(new ExternalServiceUnavailableException("Pedidos no está disponible"));

        assertThrows(ExternalServiceUnavailableException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 5, null), USUARIO));

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    // INT-02: lo mismo si falla Usuarios
    @Test
    void crearResena_conUsuariosCaido_propagaElErrorYNoGuarda() {
        when(pedidosClient.obtenerPedido(1L)).thenReturn(Optional.of(new PedidoDTO(1L, USUARIO, 10L, null, "ENTREGADO")));
        when(usuariosClient.obtenerUsuario(USUARIO)).thenThrow(new ExternalServiceUnavailableException("Usuarios no está disponible"));

        assertThrows(ExternalServiceUnavailableException.class,
                () -> reviewService.crearResena(request(1L, TipoObjetivo.COMERCIO, 5, null), USUARIO));

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    // ---------- SRV-02 (CU-03, CU-04): promedio ----------

    // DM-12: sin reseñas, promedio null y cantidad 0
    @Test
    void obtenerPromedio_sinResenas_devuelvePromedioNullYCantidadCero() {
        when(resenaRepository.findByObjetivoIdAndTipoObjetivo(10L, TipoObjetivo.COMERCIO)).thenReturn(List.of());

        RatingResponse rating = reviewService.obtenerPromedio(TipoObjetivo.COMERCIO, 10L);

        assertNull(rating.getPromedio());
        assertEquals(0L, rating.getCantidadResenas());
    }

    @Test
    void obtenerPromedio_conResenas_calculaPromedioConDosDecimales() {
        when(resenaRepository.findByObjetivoIdAndTipoObjetivo(10L, TipoObjetivo.COMERCIO))
                .thenReturn(List.of(resena(5), resena(4), resena(4)));

        RatingResponse rating = reviewService.obtenerPromedio(TipoObjetivo.COMERCIO, 10L);

        assertEquals(new BigDecimal("4.33"), rating.getPromedio());
        assertEquals(3L, rating.getCantidadResenas());
    }

    // Redondeo HALF_UP: 33 / 8 = 4.125 y debe dar 4.13 (HALF_EVEN daría 4.12)
    @Test
    void obtenerPromedio_redondeaHaciaArribaEnLaMitad() {
        when(resenaRepository.findByObjetivoIdAndTipoObjetivo(10L, TipoObjetivo.COMERCIO))
                .thenReturn(List.of(resena(5), resena(4), resena(4), resena(4), resena(4), resena(4), resena(4), resena(4)));

        RatingResponse rating = reviewService.obtenerPromedio(TipoObjetivo.COMERCIO, 10L);

        assertEquals(new BigDecimal("4.13"), rating.getPromedio());
    }
}
