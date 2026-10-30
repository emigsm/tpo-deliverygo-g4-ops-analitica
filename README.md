# DeliveryGo — Grupo 4

Módulo de **Opiniones y Analítica**: registra reseñas de comercios y repartidores,
consulta calificaciones y genera un tablero de métricas operativas.

## Tecnologías

Java 25, Spring Boot 3.5.16, Spring Data JPA, H2, Springdoc OpenAPI 2.9.1 y Maven.

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
| [CU-06](docs/casos-de-uso.md#cu-06) | Registrar evento analítico | Evolución pendiente |
| [CU-07](docs/casos-de-uso.md#cu-07) | Consultar métricas por período | Evolución pendiente |
| [CU-08](docs/casos-de-uso.md#cu-08) | Analizar cantidad y estado de pedidos | Evolución pendiente |
| [CU-09](docs/casos-de-uso.md#cu-09) | Analizar pagos aprobados y rechazados | Evolución pendiente |
| [CU-10](docs/casos-de-uso.md#cu-10) | Analizar tiempos y entregas | Evolución pendiente |

## API REST

| ID | Método y ruta | CU |
| --- | --- | --- |
| API-01 | `POST /api/v1/reviews` | CU-01, CU-02 |
| API-02 | `GET /api/v1/merchants/{id}/rating` | CU-03 |
| API-03 | `GET /api/v1/couriers/{id}/rating` | CU-04 |
| API-04 | `GET /api/v1/analytics/dashboard` | CU-05 |

[Swagger UI](http://localhost:8089/swagger-ui.html).
[Contratos, ejemplos y exportación OpenAPI](docs/api/README.md).
[OpenAPI implementado](docs/api/openapi.yaml) ·
[Evolución propuesta API-05/06](docs/api/openapi-evolucion-propuesta.yaml).
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
`X-Usuario-Id`. [Configuración y datos de ejemplo](docs/api/README.md#ejecucion-local).

- API: <http://localhost:8089/api/v1>
- OpenAPI: <http://localhost:8089/v3/api-docs.yaml>
- H2: <http://localhost:8089/h2-console>

## Integraciones externas

Usuarios, Pedidos, Comercios y Logística cuentan con adaptadores y mocks locales;
no hay clientes HTTP reales. Pagos pertenece a la evolución mediante eventos.
**INT-05 es propuesto y está pendiente de confirmación con Pedidos.**
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
