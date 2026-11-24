# DeliveryGo — Grupo 4

Módulo de **Opiniones y Analítica**: registra reseñas de comercios y repartidores,
consulta calificaciones y genera un tablero de métricas operativas.

## Tecnologías

Java 25, Spring Boot 3.5.16, Spring Data JPA, Spring Security, H2, Springdoc OpenAPI 2.9.1 y Maven.

## Arquitectura

Controller → Service → Repository, con DTOs para contratos y clientes externos
para aislar integraciones. [Arquitectura y decisiones](docs/tpo-apis-grupo-4-v1.5.md).

## Casos de uso

| CU | Nombre | Estado |
| --- | --- | --- |
| [CU-01](docs/tpo-apis-grupo-4-v1.5.md) | Calificar comercio | Implementado con mocks |
| [CU-02](docs/tpo-apis-grupo-4-v1.5.md) | Calificar repartidor | Implementado con mocks |
| [CU-03](docs/tpo-apis-grupo-4-v1.5.md) | Consultar promedio de comercio | Implementado |
| [CU-04](docs/tpo-apis-grupo-4-v1.5.md) | Consultar promedio de repartidor | Implementado |
| [CU-05](docs/tpo-apis-grupo-4-v1.5.md) | Generar tablero de métricas | Implementado con mock de Pedidos |
| [CU-06](docs/tpo-apis-grupo-4-v1.5.md) | Registrar evento analítico | Implementado |
| [CU-07](docs/tpo-apis-grupo-4-v1.5.md) | Consultar métricas por período | Implementado |
| [CU-08](docs/tpo-apis-grupo-4-v1.5.md) | Analizar cantidad y estado de pedidos | Implementado (cancelados) |
| [CU-09](docs/tpo-apis-grupo-4-v1.5.md) | Analizar pagos aprobados y rechazados | Implementado |
| [CU-10](docs/tpo-apis-grupo-4-v1.5.md) | Analizar tiempos y entregas | Evolución pendiente |

## API REST

| ID | Método y ruta | CU |
| --- | --- | --- |
| API-01 | `POST /api/v1/reviews` | CU-01, CU-02 |
| API-02 | `GET /api/v1/merchants/{id}/rating` | CU-03 |
| API-03 | `GET /api/v1/couriers/{id}/rating` | CU-04 |
| API-04 | `GET /api/v1/analytics/dashboard` | CU-05, CU-08, CU-09 |
| API-05 | `POST /api/v1/analytics/events` | CU-06 |
| API-06 | `GET /api/v1/analytics/metrics` | CU-07 |
| — | `GET /api/v1/status` | Health check |

[Swagger UI](http://localhost:8089/swagger-ui.html).
[Contratos y ejemplos](docs/tpo-apis-grupo-4-v1.5.md).
[OpenAPI implementado](docs/api/openapi.yaml).
Los errores manejados cambian de contrato: [tpo-apis-grupo-4-v1.5](docs/tpo-apis-grupo-4-v1.5.md).

## Instalación y ejecución

Requisitos: JDK 25 y Maven instalado; no se incluye Maven Wrapper.
Desde la raíz del módulo:

```sh
mvn -DskipTests compile
mvn spring-boot:run
```

La configuración local incluida usa puerto **8089**, H2 en memoria y clientes mock;
no requiere configurar URLs externas. Las APIs exigen un JWT (ver [Autenticación](#autenticación-jwt));
`GET /api/v1/status` es público.

- API: <http://localhost:8089/api/v1>
- OpenAPI: <http://localhost:8089/v3/api-docs.yaml>
- H2: <http://localhost:8089/h2-console>

## Pruebas automáticas

```sh
mvn test
```

Los tests usan JUnit 5 y MockMvc con los mocks de los otros módulos, sin servicios externos.
Cubren la seguridad (401, 403, token vencido o adulterado, `X-Api-Key`), las reglas de reseñas
(duplicada, pedido no entregado o de otro usuario) y las validaciones de eventos y períodos.

## Autenticación (JWT)

El módulo **no emite tokens**: valida el JWT que emite el módulo Usuarios (`POST /api/v1/auth/login`).
Se envía en cada petición: `Authorization: Bearer <token>`.

- Firma **HS256** con clave compartida: variable de entorno `JWT_SECRET`, que debe ser idéntica a la de Usuarios.
  Sin la variable se usa una clave de demo (`jwt.secret` en `application.properties`).
- Claims usados: `sub` (id del usuario, número), `role` (CLIENTE, COMERCIO, REPARTIDOR o ADMIN) y `exp`.
- El `usuarioId` de una reseña sale del `sub`; ya no existe el header `X-Usuario-Id`.

| Ruta | Rol requerido |
| --- | --- |
| `GET /api/v1/status` | Público |
| `POST /api/v1/reviews` | CLIENTE |
| `GET /api/v1/analytics/dashboard`, `GET /api/v1/analytics/metrics` | ADMIN |
| `POST /api/v1/analytics/events` | Módulo emisor, con header `X-Api-Key` (no sirve un JWT) |
| Ratings (`GET .../rating`) | Cualquier token válido |

Sin token o con token inválido o vencido: **401** (`code: UNAUTHENTICATED`). Token válido con rol incorrecto: **403** (`code: FORBIDDEN`). Ambos con el mismo formato JSON de error que el resto de la API.

**Eventos (API-05):** los módulos Pedidos, Pagos y Logística se identifican con `X-Api-Key`, una clave por módulo
(variables `EVENTOS_API_KEY_PEDIDOS`, `EVENTOS_API_KEY_PAGOS` y `EVENTOS_API_KEY_LOGISTICA`, mínimo 16 caracteres).
El `moduloOrigen` del evento debe ser el módulo dueño de la clave; si no, **403** (`MODULE_ORIGIN_MISMATCH`).

Para pruebas locales sin Usuarios, se puede armar un token en <https://jwt.io>: algoritmo HS256,
payload `{"sub":"100","role":"CLIENTE","exp":1893456000}` y la clave de `jwt.secret`.

## Pruebas de integración

Al levantar el módulo (`mvn spring-boot:run`) los clientes de Usuarios, Comercios, Pedidos
y Logística son mocks y se precargan dos reseñas demo (comercio 10 y repartidor 20, del
usuario 101, sin colisión con los pedidos mock). Esas reseñas cuentan en el tablero del
día de arranque. Requieren un token (ver [Autenticación](#autenticación-jwt)): `TOKEN_CLIENTE` con
`"sub":"100"` y rol CLIENTE, y `TOKEN_ADMIN` con rol ADMIN.

| Dato mock | Valor |
| --- | --- |
| Usuario activo | 100 |
| Comercio activo | 10 |
| Repartidor activo | 20 |
| Pedido 1 | usuario 100, comercio 10, ENTREGADO |
| Pedido 2 | usuario 100, repartidor 20, ENTREGADO |
| Pedido 3 | usuario 100, comercio 10, EN_CAMINO (reseña: 422) |

```sh
# Health check (público)
curl -i http://localhost:8089/api/v1/status

# Rating con la reseña precargada: cantidadResenas 1, promedio 4.00
curl -i -H "Authorization: Bearer $TOKEN_CLIENTE" http://localhost:8089/api/v1/merchants/10/rating

# Crear reseña de comercio: 201 (repetirla devuelve 409). El usuario sale del token.
curl -i -X POST http://localhost:8089/api/v1/reviews \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN_CLIENTE" \
  -d '{"pedidoId":1,"tipoObjetivo":"COMERCIO","puntuacion":5,"comentario":"Buena atención"}'

# Rating actualizado: cantidadResenas 2, promedio 4.50
curl -i -H "Authorization: Bearer $TOKEN_CLIENTE" http://localhost:8089/api/v1/merchants/10/rating

# Reseña de repartidor: mismo POST con pedidoId 2 y tipoObjetivo REPARTIDOR
# Tablero del día (solo ADMIN: con TOKEN_CLIENTE devuelve 403)
curl -i -H "Authorization: Bearer $TOKEN_ADMIN" http://localhost:8089/api/v1/analytics/dashboard
```

En PowerShell usar `curl.exe` y escapar las comillas del JSON
(`-d "{\"pedidoId\":1,\"tipoObjetivo\":\"COMERCIO\",\"puntuacion\":5}"`).

Perfil `integracion`: Comercios y Usuarios por HTTP; Pedidos y Logística siguen con mock
y no se precargan reseñas. Las URLs y la API key son obligatorias:

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=integracion \
  -Dspring-boot.run.arguments="--integraciones.comercios.url=<URL> --integraciones.usuarios.url=<URL> --integraciones.usuarios.api-key=<KEY>"
```

## Integraciones externas

Usuarios, Pedidos, Comercios y Logística cuentan con adaptadores y mocks locales;
no hay clientes HTTP reales. Los eventos de Pedidos, Pagos y Logística ingresan por
API-05 (CU-06) y se analizan en el tablero. **INT-05 es propuesto y está pendiente de
confirmación con Pedidos.**
[Detalle en el documento de APIs](docs/tpo-apis-grupo-4-v1.5.md).

## Documentación

- [TPO APIs Grupo 4 v1.5](docs/tpo-apis-grupo-4-v1.5.md): fuente de verdad (casos de uso, reglas de negocio,
  modelo, decisiones, diagramas, servicios y contratos REST). Este README no repite ese contenido.

## Equipo

Grupo 4: Alejandro Aguilera, Alejo Ferrari Cioffi, Nicolás Gabriel Hruszczak y
Emiliano Guillermo San Martin. Docente: Alejandro Luis Foglino.