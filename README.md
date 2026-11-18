# DeliveryGo — Grupo 4

Módulo de **Opiniones y Analítica**: registra reseñas de comercios y repartidores,
consulta calificaciones y genera un tablero de métricas operativas.

## Tecnologías

Java 25, Spring Boot 3.5.16, Spring Data JPA, Spring Security, H2, Springdoc OpenAPI 2.9.1 y Maven.

## Arquitectura

Controller → Service → Repository, con DTOs para contratos y clientes externos
para aislar integraciones. [Arquitectura y decisiones](docs/arquitectura.md).

## Casos de uso

| CU | Nombre | Estado |
| --- | --- | --- |
| [CU-01](docs/casos-de-uso.md#cu-01) | Calificar comercio | Implementado con mocks |
| [CU-02](docs/casos-de-uso.md#cu-02) | Calificar repartidor | Implementado con mocks |
| [CU-03](docs/casos-de-uso.md#cu-03) | Consultar promedio de comercio | Implementado |
| [CU-04](docs/casos-de-uso.md#cu-04) | Consultar promedio de repartidor | Implementado |
| [CU-05](docs/casos-de-uso.md#cu-05) | Generar tablero de métricas | Implementado con mock de Pedidos |
| [CU-06](docs/casos-de-uso.md#cu-06) | Registrar evento analítico | Implementado |
| [CU-07](docs/casos-de-uso.md#cu-07) | Consultar métricas por período | Implementado |
| [CU-08](docs/casos-de-uso.md#cu-08) | Analizar cantidad y estado de pedidos | Implementado (cancelados) |
| [CU-09](docs/casos-de-uso.md#cu-09) | Analizar pagos aprobados y rechazados | Implementado |
| [CU-10](docs/casos-de-uso.md#cu-10) | Analizar tiempos y entregas | Evolución pendiente |

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
[Contratos, ejemplos y exportación OpenAPI](docs/api/README.md).
[OpenAPI implementado](docs/api/openapi.yaml).
Los errores manejados cambian de contrato: [migración de clientes](docs/api/README.md#cambio-incompatible-del-contrato-de-errores).

## Instalación y ejecución

Requisitos: JDK 25 y Maven instalado; no se incluye Maven Wrapper.
Desde la raíz del módulo:

```sh
mvn -DskipTests compile
mvn spring-boot:run
```

La configuración local incluida usa puerto **8089**, H2 en memoria y clientes mock;
no requiere configurar URLs externas. La identidad temporal de API-01 se envía en
`X-Usuario-Id`. Las APIs usan HTTP Basic con credenciales fijas de demo
(usuario `user`, contraseña `deliverygo`; configurables con `OPS_USER` y `OPS_PASSWORD`),
sin CSRF ni sesión; `GET /api/v1/status` es público. JWT sigue pendiente.
[Configuración y datos de ejemplo](docs/api/README.md#ejecucion-local).

- API: <http://localhost:8089/api/v1>
- OpenAPI: <http://localhost:8089/v3/api-docs.yaml>
- H2: <http://localhost:8089/h2-console>

## Pruebas de integración

Al levantar el módulo (`mvn spring-boot:run`) los clientes de Usuarios, Comercios, Pedidos
y Logística son mocks y se precargan dos reseñas demo (comercio 10 y repartidor 20, del
usuario 101, sin colisión con los pedidos mock). Esas reseñas cuentan en el tablero del
día de arranque. Credenciales: `user` / `deliverygo`.

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
curl -i -u user:deliverygo http://localhost:8089/api/v1/merchants/10/rating

# Crear reseña de comercio: 201 (repetirla devuelve 409)
curl -i -u user:deliverygo -X POST http://localhost:8089/api/v1/reviews \
  -H "Content-Type: application/json" -H "X-Usuario-Id: 100" \
  -d '{"pedidoId":1,"tipoObjetivo":"COMERCIO","puntuacion":5,"comentario":"Buena atención"}'

# Rating actualizado: cantidadResenas 2, promedio 4.50
curl -i -u user:deliverygo http://localhost:8089/api/v1/merchants/10/rating

# Reseña de repartidor: mismo POST con pedidoId 2 y tipoObjetivo REPARTIDOR
# Tablero del día
curl -i -u user:deliverygo http://localhost:8089/api/v1/analytics/dashboard
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
[Contratos y acuerdos pendientes](docs/integraciones.md).

## Documentación

- [Casos de uso y trazabilidad](docs/casos-de-uso.md)
- [Reglas de negocio](docs/reglas-de-negocio.md)
- [Arquitectura y discrepancias](docs/arquitectura.md)
- [Integraciones](docs/integraciones.md)
- [API](docs/api/README.md)
- [Diagramas](docs/diagramas/README.md)
- [Referencias originales](docs/referencias/README.md)

## Equipo

Grupo 4: Alejandro Aguilera, Alejo Ferrari Cioffi, Nicolás Gabriel Hruszczak y
Emiliano Guillermo San Martin. Docente: Alejandro Luis Foglino.