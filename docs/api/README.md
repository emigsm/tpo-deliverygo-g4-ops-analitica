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

Spring Security está autoconfigurado, sin SecurityConfig propia. GET con
`Accept: application/json` sin credenciales devuelve 401. HTTP Basic o sesión
por formulario permiten acceder a Swagger y a las APIs. Usuario local `user` y
contraseña generada en el log, salvo configuración explícita del entorno.
`X-Usuario-Id` no autentica: solo API-01 lo usa como identidad de negocio.
JWT Bearer sigue pendiente; no está implementado.

CSRF permanece activo: con sesión autenticada, POST sin token válido devuelve
403 con el formato de Spring Boot. Con Basic y sin sesión/token puede observarse
401 por el despacho de error antes de autenticar. Basic por sí solo no habilita
POST. Para verificarlo manualmente, obtener `/login`, conservar la cookie y el
campo `_csrf`; enviarlo como `X-CSRF-TOKEN` en la misma sesión autenticada.
Swagger es accesible con credenciales; sus POST también requieren CSRF.
No se modificó ninguna configuración de seguridad.

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
estándar de Spring. ErrorResponseDTO contiene timestamp, status, code, message, path y correlationId;
no se promete este formato para errores del framework o de seguridad.

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

## Contratos publicados y trazabilidad

| API | CU | Estado |
| --- | --- | --- |
| API-01 | CU-01, CU-02 | Implementado |
| API-02 | CU-03 | Implementado |
| API-03 | CU-04 | Implementado |
| API-04 | CU-05 | Implementado con mock |
| API-05 | CU-06 | Propuesto |
| API-06 | CU-07 | Propuesto |

- [Contrato implementado](openapi.yaml): OpenAPI 3.1 exportado de la aplicación;
  solo cuatro operaciones. API-01 usa proveedores mock.
- [Evolución propuesta](openapi-evolucion-propuesta.yaml): API-05/06, DTO-11/12
  y DTO-10 ampliado. No se carga en Swagger ni registra endpoints.
  Sus referencias relativas a esquemas actuales requieren conservar ambos YAML juntos.
- CU-08/09/10 ampliarán API-04 según DM-18; sus métricas no existen en el enum Java actual.

## Cambio incompatible del contrato de errores

Solo las siete excepciones ya manejadas por GlobalExceptionHandler cambian:
`error` se reemplaza por `code`, `mensaje` por `message`, timestamp pasa de hora
local sin offset a Instant UTC y se agrega `correlationId`. Los consumidores
deben actualizar sus modelos y lectura de campos; no se mantienen alias antiguos.
Estados HTTP, reglas de negocio y mensajes permanecen iguales.

| Estado | code |
| --- | --- |
| 400 | INVALID_RATING o INVALID_PERIOD |
| 409 | DUPLICATE_REVIEW |
| 422 | BUSINESS_RULE_VIOLATION |
| 503 | EXTERNAL_SERVICE_UNAVAILABLE |

`X-Correlation-Id` opcional se reutiliza solo si tiene formato UUID canónico
(36 caracteres, hexadecimal sin distinguir mayúsculas y guiones); se conserva
el valor recibido. Ausente o inválido genera UUID. Solo se refleja en el cuerpo
de errores manejados; no se agrega a respuestas exitosas ni a errores de Spring.
No se registran ni devuelven trazas internas.

```json
{"timestamp":"2026-10-30T12:00:00Z","status":422,"code":"BUSINESS_RULE_VIOLATION","message":"El pedido no fue entregado","path":"/api/v1/reviews","correlationId":"2aa05de5-76a4-46e3-a7a1-e4572cf602a6"}
```

Fecha mal formada: 400 de Spring Boot con timestamp, status, error y path;
rango invertido: 400 ErrorResponseDTO. El OpenAPI representa ambos con oneOf.
Los timestamps LocalDateTime de reseñas y dashboard conservan su comportamiento
sin offset y se documentan con formato personalizado `local-date-time`.

## Regeneración

Con Java 25 y Maven, ejecutar `mvn -DskipTests compile` y `mvn spring-boot:run`.
Exportar desde otra terminal, usando la contraseña efectiva del entorno:

```sh
curl --fail --show-error --user user http://localhost:8089/v3/api-docs.yaml -o docs/api/openapi.yaml
```

No editar el YAML generado: corregir anotaciones/configuración y regenerar.
El servidor publicado refleja el puerto de captura 18092, usado para evitar
interferir con otras instancias; el puerto habitual del proyecto es 8089.
Validar OpenAPI 3.1, referencias, ejemplos y las cuatro operaciones antes de publicar.
Un validador externo puede instalarse fuera del proyecto:

```sh
python3 -m venv /tmp/deliverygo-openapi-validator
/tmp/deliverygo-openapi-validator/bin/pip install openapi-spec-validator
/tmp/deliverygo-openapi-validator/bin/openapi-spec-validator docs/api/openapi.yaml
/tmp/deliverygo-openapi-validator/bin/openapi-spec-validator docs/api/openapi-evolucion-propuesta.yaml
```

## Acuerdos pendientes de evolución

API-05: idempotencia y prevención de duplicados; autenticación servicio a
servicio; identificadores de evento/entidad y formatos definitivos; zona,
offset y significado de fechaEvento; validación de moduloOrigen contra tipoEvento.
Long y LocalDateTime son tipos provisionales del documento de endpoints, no
acuerdos finales. El contrato expresa detalle obligatorio para cambios de estado,
valor obligatorio en minutos para entregas y ninguno requerido para pagos.

API-06: confirmar si el rango contiene períodos completos, incluye solapamientos
y qué límites aplica; REP-08 sugiere contención, sin cerrar el acuerdo.
También sigue pendiente la persistencia del promedio de entrega null de CU-10
frente al NOT NULL actual. INT-05 continúa pendiente de acuerdo con Pedidos.

## Verificación de Task 05

Compilación Java 25 y Maven 3.9.9 correcta; ambos documentos OpenAPI 3.1
validados, referencias resueltas y ejemplos de respuestas cotejados con sus esquemas.
Swagger autenticado devuelve 200 y su contrato contiene exactamente cuatro operaciones.
Verificación HTTP manual: 201 comercio/repartidor; 200 ratings con/sin reseñas y
dashboard con/sin reseñas; 400 puntuación, rango invertido y fecha mal formada;
409 duplicado; 422 pedido no entregado; correlación válida reutilizada e inválida
reemplazada; 401 anónimo y 403 POST sin CSRF con sesión autenticada.
Una segunda instancia temporal con mock de Pedidos indisponible y snapshots
sembrados en H2 confirmó parcial=true y 503 sin respaldo exacto.
Sin pruebas automatizadas nuevas ni cambios de seguridad, servicios o persistencia.

## Escenarios de verificación manual

Conservar como guía funcional, sin agregar pruebas automatizadas:

- Crear reseñas válidas de comercio y repartidor; repetir para observar 409.
- Revisar puntuación inválida, pedido no entregado y propietario incorrecto.
- Consultar rating con y sin reseñas.
- Consultar tablero con fechas omitidas, inválidas y rango invertido.
- Verificar métricas con/sin reseñas y actualización sin snapshots duplicados.
- Simular caída de Pedidos con/sin respaldo del período exacto en la misma ejecución.

Estos escenarios describen verificaciones para una aplicación accesible;
no sustituyen una suite automatizada; los casos ejecutados se indican arriba.
