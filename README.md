# tpo-deliverygo-g4-ops-analitica
DeliveryGo | Módulo de Opiniones y Analítica

## Dashboard — CU-05

`GET /api/v1/analytics/dashboard?periodoDesde=2026-10-01&periodoHasta=2026-10-30`

Ambos parámetros son opcionales, con formato ISO `yyyy-MM-dd`. Cada extremo
omitido toma la fecha actual del servidor, leída una sola vez por solicitud.
El período incluye ambos días. Las fechas mal formadas y los rangos invertidos
responden HTTP 400; Spring conserva su manejo estándar para errores de formato.

La respuesta contiene `generadoEn`, `metricas` y `parcial`. Cada métrica informa
`fuente`, `tipoMetrica`, `periodoDesde`, `periodoHasta` y `valor`:

- `RESENAS / CANTIDAD_RESENAS`: cantidad de reseñas del período.
- `RESENAS / PROMEDIO_CALIFICACION`: promedio con dos decimales, redondeo HALF_UP.
  Sin reseñas, se devuelve `valor: null` y no se persiste ese promedio.
- `PEDIDOS / PEDIDOS_ENTREGADOS`: cantidad agregada obtenida mediante PedidosClient.

RN-03: cada snapshot tiene fuente y período obligatorios. RN-06: se actualiza el
snapshot de la misma fuente, tipo y período, conservando la restricción UNIQUE
existente. No se agregan locks ni reintentos; solicitudes concurrentes pueden
producir un conflicto de persistencia, aunque la base impide duplicados.

Si Pedidos está indisponible o devuelve estadísticas inválidas, se busca el
snapshot de PEDIDOS_ENTREGADOS para el período **exacto** solicitado:

- Con snapshot válido: HTTP 200, `parcial: true`, con el valor persistido.
  La lectura del respaldo no modifica su fecha de cálculo.
- Sin snapshot válido: HTTP 503 con ErrorResponseDTO. No se inventan valores ni
  se utilizan períodos distintos. La transacción revierte las escrituras de esa solicitud.

`parcial: false` indica que Pedidos respondió correctamente. `generadoEn` fecha
la respuesta, no la antigüedad del respaldo. La configuración actual de H2 en
memoria con `create-drop` pierde los snapshots al reiniciar la aplicación.

## INT-05 — contrato propuesto, pendiente de confirmación

`GET /api/v1/orders/stats?estado=ENTREGADO&periodoDesde=yyyy-MM-dd&periodoHasta=yyyy-MM-dd`

Respuesta propuesta (`OrdersStatsDTO`):

```json
{
  "estado": "ENTREGADO",
  "periodoDesde": "2026-10-01",
  "periodoHasta": "2026-10-30",
  "cantidad": 2
}
```

Se valida respuesta no nula, cantidad no nula y no negativa, estado y período
coincidentes. La indisponibilidad se comunica mediante
ExternalServiceUnavailableException. No se consultan pedidos individuales para
calcular estadísticas. Todavía no existe un cliente HTTP real.

Pendiente de acordar con el equipo de Pedidos: nombres de parámetros, literal de
estado, JSON, fecha utilizada para contar entregas, límites inclusivos y zona
horaria, autenticación, URL, tiempos de espera y errores.

El mock sigue activo y sus estadísticas se configuran en application.properties:

```properties
app.pedidos.mock.stats.disponible=true
app.pedidos.mock.stats.cantidad-entregados=2
```

La cantidad es un agregado simulado para cualquier período solicitado, no un
conteo histórico real. `disponible=false` o una cantidad negativa simulan
indisponibilidad. Estas opciones no alteran obtenerPedido ni CU-01 a CU-04.
Las propiedades se leen al iniciar; reiniciar también elimina los snapshots H2.
Para verificar fallback durante la misma ejecución, se necesita sembrar un
snapshot del período o cambiar la disponibilidad del mock en el depurador.

## Arquitectura y verificación

CU-05 mantiene Controller → Service → Repository, con la integración externa
aislada en PedidosClient. No incorpora CU-06 a CU-10 ni modifica reglas de reseñas.
Swagger describe parámetros, métricas y respuestas 200/400/503.

Compilación sin ejecutar pruebas: `mvn -DskipTests compile` (Java 25).
No se agregan pruebas automatizadas. Verificar manualmente fechas omitidas e
inválidas, métricas con/sin reseñas, actualización sin duplicados y caída de
Pedidos con/sin respaldo del período exacto.
