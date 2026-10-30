# API implementada y OpenAPI

<a id="ejecucion-local"></a>
## Ejecución local

Desde la raíz del módulo, con Java 25 y Maven:

```sh
mvn -DskipTests compile
mvn spring-boot:run
```

La configuración incluida usa puerto 8089, H2 en memoria, usuario `sa`, contraseña
vacía y URL JDBC `jdbc:h2:mem:deliverygo-ops-analitica`. Consola:
<http://localhost:8089/h2-console>. Los datos se eliminan al reiniciar.
No hay URLs de proveedores que configurar mientras se usen mocks.

Spring Security está entre las dependencias y no existe SecurityConfig propia:
la autoconfiguración puede exigir el usuario local `user` y la contraseña generada
en el log de inicio, además del header de negocio. Puede proteger Swagger/H2 o
rechazar POST por CSRF; X-Usuario-Id no sustituye esa autenticación. No se promete
acceso anónimo ni autenticación JWT. Usar las credenciales efectivas del entorno;
esta tarea no cambia seguridad ni configuración.

Los mocks contienen usuario 100, comercio 10 y repartidor 20 activos. Pedido 1:
usuario 100, comercio 10, ENTREGADO; pedido 2: usuario 100, repartidor 20,
ENTREGADO; pedido 3: usuario 100, comercio 10, EN_CAMINO. Los pedidos 1/2 permiten
probar únicamente su objetivo correspondiente; no tienen ambos objetivos poblados.

## API-01 — Crear reseña

`POST /api/v1/reviews`, header temporal obligatorio `X-Usuario-Id: 100`.
DTO-01 ResenaRequest → DTO-02 ResenaResponse. Ejemplo de body para comercio:

```json
{"pedidoId":1,"tipoObjetivo":"COMERCIO","puntuacion":5,"comentario":"Buena atención"}
```

Para repartidor usar pedidoId 2 y tipoObjetivo REPARTIDOR. usuarioId se toma del
header y objetivoId se deriva del pedido: no se reciben en el body (DM-13 parcial).
Respuesta 201: id, usuarioId, pedidoId, tipoObjetivo, objetivoId, puntuacion,
comentario y fechaCreacion. Errores de negocio 400/409/422/503 según las fichas
[CU-01 y CU-02](../casos-de-uso.md#cu-01). No hay PUT ni DELETE.

## API-02 y API-03 — Calificación promedio

`GET /api/v1/merchants/{id}/rating` y `GET /api/v1/couriers/{id}/rating`.
DTO-03 RatingResponse: objetivoId, tipoObjetivo, promedio y cantidadResenas.
Sin reseñas: 200, promedio null y cantidadResenas 0. Promedio HALF_UP con dos
decimales. Consultan datos locales, sin validar existencia externa ni propietario.

## API-04 — Tablero

`GET /api/v1/analytics/dashboard?periodoDesde=2026-10-01&periodoHasta=2026-10-30`.
Fechas opcionales ISO yyyy-MM-dd; cada extremo omitido toma hoy, leído una vez
por solicitud. Ambos días son inclusivos. DTO-04 DashboardResponse contiene
generadoEn, metricas y parcial. Cada DTO-10 MetricaResumen tiene fuente,
tipoMetrica, periodoDesde, periodoHasta y valor.

| Fuente / tipo | Resultado |
| --- | --- |
| RESENAS / CANTIDAD_RESENAS | Cantidad del período |
| RESENAS / PROMEDIO_CALIFICACION | Promedio HALF_UP con dos decimales; null sin reseñas, sin persistir ese promedio |
| PEDIDOS / PEDIDOS_ENTREGADOS | Agregado simulado mediante INT-05 |

Rango invertido: 400 con ErrorResponseDTO. Fecha mal formada: 400 con manejo
estándar de Spring. ErrorResponseDTO contiene timestamp, status, error, mensaje
y path; no se promete este formato para todos los errores del framework.

Pedidos indisponible o inválido: se usa solo el snapshot válido del período
exacto y parcial=true; no se modifica su fecha de cálculo. Sin respaldo válido:
503 y rollback. generadoEn no expresa antigüedad del respaldo. parcial=false
indica respuesta válida del cliente de Pedidos, actualmente mock.

Las propiedades existentes `app.pedidos.mock.stats.disponible=true` y
`app.pedidos.mock.stats.cantidad-entregados=2` se leen al iniciar. La cantidad
es simulada para cualquier período; false o cantidad negativa provocan
indisponibilidad. No afectan obtenerPedido ni CU-01 a CU-04. Reiniciar elimina
snapshots H2; para observar respaldo durante la misma ejecución se requiere
sembrar un snapshot o cambiar la disponibilidad del mock mediante depurador.

## Verificación de la especificación disponible

Se contrastó `target/openapi-verified.json` con OpinionesController, DTOs,
AnalyticsService y GlobalExceptionHandler actuales. Incluye las cuatro rutas,
ambas fechas opcionales, las tres métricas actuales, promedio nullable,
respaldo del período exacto, parcial y 503 sin respaldo. No incluye API-05/06.
Su servidor de captura es `http://127.0.0.1:18089`, distinto del puerto local
configurado 8089; no es un endpoint externo confirmado.

**El archivo refleja el CU-05 actual, pero no es un contrato preciso para publicar:**
la respuesta 400 de API-04 referencia DashboardResponse, mientras el rango
invertido devuelve ErrorResponseDTO y los errores de formato usan Spring.
La anotación actual no declara contenido específico para 400 y el generador
infiere el esquema del retorno. Regenerar sin corregir anotaciones puede repetir
esa discrepancia. No se modifica Java ni se corrige manualmente el contrato.
Por ello no se publica `openapi.yaml` inexacto; queda pendiente de regeneración
y validación. El JSON de target es un artefacto local ignorado por Git.

## Regeneración pendiente

Con la aplicación ejecutándose, exportar primero a un archivo temporal:

```sh
curl --fail --show-error http://localhost:8089/v3/api-docs.yaml -o /tmp/deliverygo-openapi.yaml
```

Si Spring Security solicita credenciales, repetir con `--user user` para que curl
solicite la contraseña del entorno. Swagger UI:
<http://localhost:8089/swagger-ui.html>. JSON generado:
<http://localhost:8089/v3/api-docs>.

Antes de copiar la exportación a `docs/api/openapi.yaml`, verificar rutas,
DTOs, parámetros, enums, nullabilidad y esquemas de errores contra Java;
confirmar especialmente 400 de API-04. Si persiste la discrepancia, mantener
la exportación fuera de la documentación publicada y coordinar su corrección
en una tarea de código separada. Conservar la salida generada sin inventar
contratos ni incorporar API-05/06 o métricas futuras.

## Evolución sin contrato activo

API-05 `POST /api/v1/analytics/events` y API-06
`GET /api/v1/analytics/metrics` son propuestas de las referencias, no endpoints
implementados. Sus fichas están en [casos de uso](../casos-de-uso.md#cu-06).

## Escenarios de verificación manual

Conservar como guía funcional, sin agregar pruebas automatizadas:

- Crear reseñas válidas de comercio y repartidor; repetir para observar 409.
- Revisar puntuación inválida, pedido no entregado y propietario incorrecto.
- Consultar rating con y sin reseñas.
- Consultar tablero con fechas omitidas, inválidas y rango invertido.
- Verificar métricas con/sin reseñas y actualización sin snapshots duplicados.
- Simular caída de Pedidos con/sin respaldo del período exacto en la misma ejecución.

Estos escenarios describen verificaciones para una aplicación accesible;
no se ejecutaron como parte de esta modificación documental.
