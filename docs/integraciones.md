# Integraciones

## Adaptadores salientes

DM-06 mantiene referencias por identificador y DM-11 aísla proveedores detrás de
interfaces. Todas las implementaciones inyectadas actuales son mocks `@Component`:
no hay HTTP real, URLs externas, autenticación ni timeouts configurados.

| INT | Proveedor y propósito | CU | Endpoint esperado | DTO | Estado |
| --- | --- | --- | --- | --- | --- |
| INT-01 | Pedidos: estado, propietario y objetivo del pedido | CU-01, CU-02 | GET /api/v1/orders/{id} | DTO-05 PedidoDTO: id, usuarioId, comercioId, repartidorId, estado | PedidosClient / PedidosClientMock; contrato esperado, conexión real pendiente |
| INT-02 | Usuarios: existencia y actividad | CU-01, CU-02 | GET /api/v1/users/{id} | DTO-06 UsuarioDTO: id, activo | UsuariosClient / UsuariosClientMock; conexión real pendiente |
| INT-03 | Comercios: existencia y actividad | CU-01 | GET /api/v1/merchants/{id} | DTO-07 ComercioDTO: id, activo | ComercioClient / ComercioClientMock; propuesto, por acordar (DM-07) |
| INT-04 | Logística: existencia y actividad | CU-02 | GET /api/v1/couriers/{id} | DTO-08 RepartidorDTO: id, activo | LogisticaClient / LogisticaClientMock; propuesto, por acordar (DM-07) |
| INT-05 | Pedidos: conteo agregado de entregados | CU-05 | GET /api/v1/orders/stats | DTO-09 OrdersStatsDTO: estado, periodoDesde, periodoHasta, cantidad | **Propuesto, pendiente de confirmación con el equipo de Pedidos**; solo mock |

INT-01 a INT-04 esperan conceptualmente 200, 404 (Optional vacío) y 503
(ExternalServiceUnavailableException). No son contratos remotos verificados.
La traducción a la API propia se describe en [casos de uso](casos-de-uso.md).

## INT-05 propuesto

```text
GET /api/v1/orders/stats?estado=ENTREGADO&periodoDesde=yyyy-MM-dd&periodoHasta=yyyy-MM-dd
```

Respuesta propuesta, no contrato confirmado:

```json
{"estado":"ENTREGADO","periodoDesde":"2026-10-01","periodoHasta":"2026-10-30","cantidad":2}
```

AnalyticsService exige respuesta y cantidad no nulas, cantidad no negativa,
estado ENTREGADO y período coincidente. Consulta exclusivamente estadísticas
agregadas: no recorre pedidos individuales. El mock devuelve una cantidad
configurada para cualquier período, no un conteo histórico.

Pendiente con Pedidos: nombres de parámetros y campos, literal de estado,
fecha usada para contar entregas, límites inclusivos y zona horaria,
autenticación, URL, timeouts y errores. El respaldo ante caída se documenta
en [CU-05](casos-de-uso.md#cu-05).

## Eventos de evolución

La evolución propone que Pedidos, Pagos y Logística envíen eventos a
`POST /api/v1/analytics/events` (API-05, CU-06). No hay endpoint activo ni
nuevos clientes salientes; no se asignan identificadores INT adicionales.

| Emisor | CU consumidores | Evento previsto | DTO esperado | Estado |
| --- | --- | --- | --- | --- |
| Pedidos | CU-06, CU-08 | PEDIDO_ESTADO_CAMBIADO; detalle indica CANCELADO para CU-08 | DTO-11 → DTO-12 | Propuesto, pendiente de implementación y acuerdo |
| Pagos | CU-06, CU-09 | PAGO_APROBADO / PAGO_RECHAZADO | DTO-11 → DTO-12 | Propuesto, pendiente de implementación y acuerdo |
| Logística | CU-06, CU-10 | ENTREGA_COMPLETADA; valor expresa minutos | DTO-11 → DTO-12 | Propuesto, pendiente de implementación y acuerdo |

DTO-11 previsto: tipoEvento, moduloOrigen, entidadId, valor opcional, detalle
opcional y fechaEvento; DTO-12 agrega id. El documento de endpoints propone
invocación servicio a servicio sin X-Usuario-Id; la autenticación real entre
módulos requiere confirmación. También faltan acuerdos de reintentos,
duplicados, entrega de eventos y zona horaria; no se inventan políticas.

CU-07 lee snapshots locales y CU-08/09/10 consumen eventos locales; no requieren
consultas HTTP nuevas según la evolución. No se propone un endpoint de
estadísticas de Pagos ni de Logística.
