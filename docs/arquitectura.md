# Arquitectura

## Capas y responsabilidades actuales

Controller → Service → Repository. El servicio también coordina clientes externos;
los DTOs transportan contratos y el mapper convierte reseñas. No se accede a bases
ajenas. [Diagramas originales](diagramas/README.md) y [fuentes](referencias/README.md).

| Componente | Responsabilidad y referencias |
| --- | --- |
| OpinionesController | API-01 a API-04; delega CU-01 a CU-05 |
| ReviewService | SRV-01 crearResena; SRV-02 obtenerPromedio; reglas de reseñas y consultas |
| AnalyticsService | SRV-03 generarTablero(desde, hasta); cálculo, snapshots y respaldo |
| ResenaRepository | REP-01 existsByUsuarioIdAndPedidoIdAndTipoObjetivo; REP-02 save; REP-03 findByObjetivoIdAndTipoObjetivo; consulta por fecha de CU-05 sin nuevo ID oficial |
| MetricaRepository | REP-04 save / recuperación histórica; REP-05 búsqueda por fuente, tipo y período exacto |
| ResenaMapper | Convierte ENT-01 y DTO-01/02 |
| Clientes | PedidosClient, UsuariosClient, ComercioClient y LogisticaClient; implementaciones mock |
| GlobalExceptionHandler | Traduce excepciones de negocio/integración a ErrorResponseDTO |
| OpenApiConfig | Metadatos de la documentación Springdoc |

ErrorResponseDTO no tiene un ID DTO asignado en las fuentes; no se inventa uno.
El método findFirstByFuenteAndTipoMetricaOrderByFechaCalculoDesc existe, pero
el tablero usa REP-05 para el respaldo del período exacto.

## Dominio y persistencia

| ID | Entidad y atributos actuales |
| --- | --- |
| ENT-01 | Resena: id Long, usuarioId, pedidoId, tipoObjetivo COMERCIO/REPARTIDOR, objetivoId, puntuacion, comentario opcional (longitud 1000), fechaCreacion |
| ENT-02 | Metrica: id Long, fuente RESENAS/PEDIDOS, tipoMetrica, periodoDesde, periodoHasta, valor BigDecimal (19,4), fechaCalculo |

TipoMetrica actual contiene PROMEDIO_CALIFICACION, CANTIDAD_RESENAS y
PEDIDOS_ENTREGADOS. Las entidades usan IDENTITY; los enums se persisten como texto.
REL-01 vincula conceptualmente reseña y pedido; REL-02, usuario; REL-03, comercio;
REL-04, repartidor. Son identificadores externos sin asociaciones JPA ni FK.
No hay relación persistente entre Resena y Metrica.

H2 usa `jdbc:h2:mem:deliverygo-ops-analitica`, usuario `sa`, contraseña vacía y
`create-drop`: reseñas y snapshots se pierden al reiniciar. No se necesitan
migraciones para ejecutar la configuración incluida.

Resena impide actualizaciones de sus columnas y tiene UNIQUE por usuario,
pedido y tipo. Metrica exige fuente, tipo, período, valor y fecha de cálculo;
su UNIQUE cubre fuente/tipo/período (RN-06). SRV-03 actualiza el snapshot existente
o crea uno. No hay locks ni reintentos; la restricción evita duplicados, pero
solicitudes concurrentes pueden producir conflicto de persistencia.

SRV-01 y SRV-03 son transaccionales; SRV-02 es de solo lectura. El tablero
calcula reseñas en [desde al inicio del día, hasta + 1 día al inicio), con ambos
días incluidos. Ante 503 sin respaldo, se revierten las escrituras de esa solicitud.
No se persiste promedio null cuando no hay reseñas. El respaldo de Pedidos no
actualiza su fecha de cálculo. Detalles de respuesta: [API](api/README.md).

## Decisiones del diseño

Síntesis de las decisiones originales, sin sustituir sus definiciones en las
[referencias](referencias/README.md).

| DM | Decisión | Situación |
| --- | --- | --- |
| DM-01 | Una reseña con discriminador tipoObjetivo | Implementada |
| DM-02 | Una opinión por usuario, pedido y tipo de objetivo | Implementada |
| DM-03 | Sin edición ni borrado de reseñas | Implementada |
| DM-04 | Validar pedido entregado únicamente mediante Pedidos | Implementada con mock |
| DM-05 | Rating on-demand y métrica persistida | Implementada; tablero calcula y persiste bajo demanda |
| DM-06 | Referencias externas por ID y DTO transitorio | Implementada |
| DM-07 | Proponer consultas de comercio/repartidor, por acordar | Mocks; acuerdos pendientes |
| DM-08 | Tablero usa agregado de Pedidos INT-05, no pedidos individuales | Implementada con mock; contrato pendiente |
| DM-09 | Respaldo por snapshot ante caída de Pedidos | Diferencia de alcance descrita abajo |
| DM-10 | Separar ReviewService y AnalyticsService por subdominio | Implementada |
| DM-11 | Adapter por cliente, con mocks temporales | Interfaces y mocks; HTTP pendiente |
| DM-12 | Rating sin reseñas: 200, cantidad 0 y promedio null | Implementada |
| DM-13 | usuarioId de JWT y objetivoId de PedidoDTO, fuera del body | Objetivo derivado; JWT pendiente |
| DM-14 | EventoAnalítico único con discriminador tipoEvento | Evolución pendiente |
| DM-15 | moduloOrigen enum PEDIDOS/PAGOS/LOGISTICA | Evolución pendiente |
| DM-16 | Tablero recibe período opcional, por defecto hoy | Implementada en CU-05; extensiones pendientes |
| DM-17 | Cancelados mediante TipoMetrica, sin detalle en Metrica | Evolución pendiente |
| DM-18 | CU-08/09/10 enriquecen API-04, sin endpoints propios | Evolución pendiente |

## Componentes previstos para evolución

ENT-03 EventoAnalítico no existe todavía. El diseño prevé id, tipoEvento,
moduloOrigen, entidadId, valor, detalle y fechaEvento, sin relación persistente
con Metrica. DTO-11/12, API-05/06, SRV-04 a SRV-08 y REP-06 a REP-08 también son
previstos; sus flujos y nombres están en [casos de uso](casos-de-uso.md).

| ID previsto | Operación prevista |
| --- | --- |
| SRV-04 | registrarEvento(eventoAnaliticoRequest) |
| SRV-05 | consultarMetricasPorPeriodo(periodoDesde, periodoHasta) |
| SRV-06 | analizarPedidosCancelados(periodoDesde, periodoHasta) |
| SRV-07 | analizarPagos(periodoDesde, periodoHasta) |
| SRV-08 | analizarEntregas(periodoDesde, periodoHasta) |
| REP-06 | EventoAnaliticoRepository.save |
| REP-07 | EventoAnaliticoRepository.findByTipoEventoAndFechaEventoBetween |
| REP-08 | MetricaRepository.findByPeriodoDesdeGreaterThanEqualAndPeriodoHastaLessThanEqual |

REP-06 persistiría eventos, REP-07 los consultaría por tipo y fecha, REP-08 leería
snapshots contenidos en un rango. La evolución no introduce acceso a bases
externas ni nuevos clientes HTTP. SecurityConfig, IntegrationClientsConfig,
AnalyticsController, MetricaMapper y EventoAnaliticoMapper figuran en el diseño
pero no existen en el código actual.

## Discrepancias

| Fuente o decisión aprobada | Evidencia actual / pendiente |
| --- | --- |
| DM-13 obtiene usuarioId de JWT | API-01 recibe X-Usuario-Id temporal; no hay integración JWT implementada ni SecurityConfig propia. Tener dependencias Security/JJWT no implementa autenticación del ecosistema. |
| AnalyticsController separado | OpinionesController contiene también API-04. |
| DM-09 y SEC-05: último snapshot y evitar fallo | Se exige respaldo del período exacto y valor no negativo; sin respaldo se responde 503. No se cambia la decisión original ni se oculta esta diferencia. |
| RN-04 incluye CHECK en la tabla del TPO | La entidad no declara CHECK para puntuación; SRV-01 valida 1–5. |
| TPO relaciona CU-05 con REP-03 e incluye referencias residuales a INT-01 / DTO-05 | El código consulta reseñas por fecha e INT-05 para agregados. REP-03 sigue siendo consulta por objetivo; no se reasigna su identificador. |
| ENT-02 descrita como snapshot “no se recalcula on-demand” | API-04 calcula y actualiza snapshots bajo demanda; DM-16 de evolución contempla recálculo por período. |
| Documento de endpoints enumera fuentes PAGOS/LOGISTICA, métricas futuras y API-05/06 | No existen en el Java actual; son evolución pendiente. |
| CU-10 sin entregas: promedio null | ENT-02.valor es NOT NULL; falta acordar cómo persistir o representar ese caso. |
| DTOs y diseño usan Reseña / cantidadReseñas | Java y JSON usan Resena / cantidadResenas; conservar nombres reales en contratos. |
| DTO-01 describe campos obligatorios en documento de endpoints | No hay restricciones Bean Validation en el DTO. Puntuación se valida en servicio; no se promete validación completa de campos nulos. |
| OpenAPI generado para 400 de API-04 | Corregido en Task 05: oneOf distingue ErrorResponseDTO del error de binding de Spring Boot; ver guía API. |
| Portada versión 1.0 | Historial hasta v1.2; evolución propone v1.3. Originales preservados. |

## Información pendiente de confirmación

- Contratos reales, URLs, autenticación, timeouts y errores de INT-01 a INT-05; INT-03/04/05 requieren acuerdo explícito.
- Autenticación JWT y autorización por actor: actores funcionales no implican controles de rol ya implementados.
- Entrega y deduplicación de eventos, zona horaria y caso de promedio null de CU-10.
- SEC-06 a SEC-10 y actualización de diagramas históricos para reflejar diferencias aprobadas posteriormente.
- Resolución de discrepancias funcionales con el equipo/docente. Las anotaciones OpenAPI y el contrato de errores fueron corregidos en Task 05; ver documentación API.
