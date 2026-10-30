# Casos de uso

Diseño: [TPO corregido y evolución](referencias/README.md). Estado: código Java
actual del repositorio. “Implementado con mocks” describe funcionalidad local,
no integración productiva. CU-06 a CU-10 no están implementados, aunque API-04
ya recibe un período: eso no implementa la consulta de snapshots de CU-07.

## Trazabilidad

Los identificadores conservan el significado de las fuentes. Los elementos de
las filas CU-06 a CU-10 son previstos, salvo los componentes compartidos existentes.
RN se define únicamente en [reglas de negocio](reglas-de-negocio.md).

| CU | RN | ENT / REL | SEC | SRV | REP | API | DTO | INT |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| CU-01 | RN-01,RN-02,RN-04,RN-05,RN-07,RN-08 | ENT-01 / REL-01,REL-02,REL-03 | SEC-01 | SRV-01 | REP-01,REP-02 | API-01 | DTO-01,DTO-02,DTO-05,DTO-06,DTO-07 | INT-01,INT-02,INT-03 |
| CU-02 | RN-01,RN-02,RN-04,RN-05,RN-07,RN-08 | ENT-01 / REL-01,REL-02,REL-04 | SEC-02 | SRV-01 | REP-01,REP-02 | API-01 | DTO-01,DTO-02,DTO-05,DTO-06,DTO-08 | INT-01,INT-02,INT-04 |
| CU-03 | No aplica | ENT-01 / REL-03 | SEC-03 | SRV-02 | REP-03 | API-02 | DTO-03 | No aplica |
| CU-04 | No aplica | ENT-01 / REL-04 | SEC-04 | SRV-02 | REP-03 | API-03 | DTO-03 | No aplica |
| CU-05 | RN-03,RN-06 | ENT-01,ENT-02 | SEC-05 | SRV-03 | REP-03,REP-04,REP-05 (*) | API-04 | DTO-04,DTO-10 | INT-05 |
| CU-06 | RN-09 | ENT-03 | SEC-06 | SRV-04 | REP-06 | API-05 | DTO-11,DTO-12 | No aplica |
| CU-07 | No aplica | ENT-02 | SEC-07 | SRV-05 | REP-08 | API-06 | DTO-10 | No aplica |
| CU-08 | RN-03,RN-06,RN-09 | ENT-02,ENT-03 | SEC-08 | SRV-06 | REP-04,REP-07 | API-04 | DTO-04,DTO-10 | No aplica |
| CU-09 | RN-03,RN-06,RN-09 | ENT-02,ENT-03 | SEC-09 | SRV-07 | REP-04,REP-07 | API-04 | DTO-04,DTO-10 | No aplica |
| CU-10 | RN-03,RN-06,RN-09 | ENT-02,ENT-03 | SEC-10 | SRV-08 | REP-04,REP-07 | API-04 | DTO-04,DTO-10 | No aplica |

(*) REP-03 se conserva como referencia del TPO; la implementación de CU-05
consulta por fecha, no por objetivo. Véase [discrepancias](arquitectura.md#discrepancias).
Los nombres y métodos se encuentran en [arquitectura](arquitectura.md), los
contratos en [API](api/README.md), los adaptadores en [integraciones](integraciones.md)
y las secuencias existentes o pendientes en [diagramas](diagramas/README.md).

<a id="cu-01"></a>
## CU-01 — Calificar comercio

- **Actor y objetivo:** Cliente; registrar una calificación del comercio de un pedido entregado.
- **Precondiciones:** pedido existente, entregado y del cliente; usuario y comercio existentes y activos; sin reseña del mismo usuario/pedido/tipo COMERCIO.
- **Flujo principal:** el Portal envía API-01; SRV-01 obtiene el pedido (INT-01), valida estado y propietario, deriva comercioId, valida usuario y comercio (INT-02/03), descarta duplicado (REP-01), valida puntuación y persiste (REP-02). Devuelve 201.
- **Alternativas:** pedido inexistente/no entregado, propietario incorrecto o entidad inexistente/inactiva: 422; duplicado: 409; puntuación nula o fuera de 1–5: 400; dependencia indisponible: 503.
- **Resultado:** ENT-01 persistida de tipo COMERCIO, devuelta como DTO-02.
- **Referencias:** fila CU-01 de la matriz; entrada DTO-01, SEC-01 existente; DM-01/02/03/04/06/07/11/13.
- **Estado:** implementado con mocks. Identidad temporal por header; JWT pendiente.

<a id="cu-02"></a>
## CU-02 — Calificar repartidor

- **Actor y objetivo:** Cliente; registrar una calificación del repartidor de un pedido entregado.
- **Precondiciones:** pedido existente, entregado y del cliente; usuario y repartidor existentes y activos; sin reseña del mismo usuario/pedido/tipo REPARTIDOR.
- **Flujo principal:** API-01 llega a SRV-01; consulta INT-01, valida estado y propietario, deriva repartidorId, consulta INT-02/04, verifica REP-01, valida puntuación y guarda por REP-02. Devuelve 201.
- **Alternativas:** las mismas de CU-01, aplicadas al repartidor.
- **Resultado:** ENT-01 persistida de tipo REPARTIDOR, devuelta como DTO-02.
- **Referencias:** fila CU-02 de la matriz; entrada DTO-01, SEC-02 existente; DM-01/02/03/04/06/07/11/13.
- **Estado:** implementado con mocks e identidad temporal por header.

<a id="cu-03"></a>
## CU-03 — Consultar promedio de comercio

- **Actor y objetivo:** Cliente / Comercio / Portal; obtener promedio y cantidad de reseñas de un comercio.
- **Precondiciones:** ninguna; puede no tener reseñas.
- **Flujo principal:** API-02 delega en SRV-02 con COMERCIO e id; REP-03 obtiene reseñas, el servicio calcula promedio y cantidad y devuelve 200 con DTO-03.
- **Alternativa:** sin reseñas devuelve 200, promedio null y cantidadResenas 0 (DM-12).
- **Resultado:** agregado actual, promedio con dos decimales y HALF_UP; no se persiste.
- **Referencias:** fila CU-03; sin RN ni INT; SEC-03 existente, DM-05/12.
- **Estado:** implementado; no consulta al módulo Comercios para validar el id.

<a id="cu-04"></a>
## CU-04 — Consultar promedio de repartidor

- **Actor y objetivo:** Repartidor / Portal; obtener promedio y cantidad de sus reseñas.
- **Precondiciones:** ninguna.
- **Flujo principal:** API-03 delega en SRV-02 con REPARTIDOR e id; REP-03 obtiene reseñas, se calcula el agregado y devuelve 200 con DTO-03.
- **Alternativa:** sin reseñas devuelve promedio null y cantidadResenas 0 (DM-12).
- **Resultado:** agregado actual con dos decimales y HALF_UP, sin persistir el promedio.
- **Referencias:** fila CU-04; sin RN ni INT; SEC-04 existente, DM-05/12.
- **Estado:** implementado; no valida existencia ni restringe la consulta al propietario del id.

<a id="cu-05"></a>
## CU-05 — Generar tablero de métricas

- **Actor y objetivo:** Analista, también Administrador vía Portal; consolidar métricas operativas.
- **Precondiciones:** ninguna en la ficha funcional. Para responder durante una caída de Pedidos se requiere respaldo válido del período exacto.
- **Flujo principal actual:** API-04 recibe fechas opcionales; SRV-03 toma hoy para cada extremo omitido, valida orden y consulta reseñas del período. Calcula cantidad y promedio, persiste/actualiza snapshots por REP-05 y REP-04, consulta INT-05 y agrega pedidos entregados. Devuelve 200 con DTO-04 y DTO-10.
- **Alternativas:** sin reseñas, cantidad 0 y promedio null sin persistir ese promedio; fechas inválidas o rango invertido: 400. Pedidos indisponible o respuesta inválida: respaldo del período exacto y parcial=true; sin respaldo válido: 503 y rollback de las escrituras de la solicitud.
- **Resultado:** tres métricas del período inclusivo; promedio HALF_UP a dos decimales. generadoEn fecha la respuesta, no la antigüedad del respaldo.
- **Referencias:** fila CU-05; RN-03/06, SEC-05 existente, DM-05/08/09/10/11 y período de DM-16 ya implementado.
- **Estado:** implementado con estadísticas simuladas de Pedidos; INT-05 sin confirmar. Las diferencias con el flujo original se conservan en arquitectura.

<a id="cu-06"></a>
## CU-06 — Registrar evento analítico

- **Actor y objetivo:** Módulo emisor; persistir eventos operativos para su análisis posterior.
- **Precondiciones:** tipoEvento, moduloOrigen, entidadId y fechaEvento presentes (RN-09).
- **Flujo previsto:** un módulo emite API-05; SRV-04 valida RN-09, guarda ENT-03 mediante REP-06 y devuelve 201 con DTO-12 a partir de DTO-11.
- **Alternativa prevista:** campos obligatorios faltantes: 400.
- **Resultado esperado:** evento persistido, independiente de ENT-01 y ENT-02.
- **Referencias:** fila CU-06, SEC-06 pendiente, DM-14/15. Integración entrante de eventos descrita en integraciones; no se define nueva INT saliente.
- **Estado:** evolución pendiente; no existen endpoint, entidad, DTO ni repositorio de eventos.

<a id="cu-07"></a>
## CU-07 — Consultar métricas por período

- **Actor y objetivo:** Analista / Portal; leer snapshots ya calculados dentro de un rango.
- **Precondiciones:** ninguna; puede no haber snapshots.
- **Flujo previsto:** API-06 con ambas fechas obligatorias llama SRV-05; REP-08 lee ENT-02 y devuelve 200 con lista de DTO-10, sin recalcular.
- **Alternativa prevista:** sin snapshots, lista vacía y 200.
- **Resultado esperado:** métricas persistidas del rango solicitado.
- **Referencias:** fila CU-07; sin RN en la ficha ni INT, SEC-07 pendiente, DM-16. REP-08 sugiere períodos contenidos en el rango; la semántica de filtrado queda pendiente de confirmación.
- **Estado:** evolución pendiente; las fechas opcionales del tablero no equivalen a esta consulta.

<a id="cu-08"></a>
## CU-08 — Analizar cantidad y estado de pedidos

- **Actor y objetivo:** Analista vía tablero; incorporar cantidad de pedidos cancelados según la evolución aportada.
- **Precondiciones:** ninguna.
- **Flujo previsto:** API-04 ampliada llama SRV-06; REP-07 consulta ENT-03 por PEDIDO_ESTADO_CAMBIADO, detalle CANCELADO y período; persiste/actualiza CANTIDAD_PEDIDOS_CANCELADOS en ENT-02 con REP-04 y lo incluye en DTO-04/10.
- **Alternativa prevista:** sin cancelaciones, snapshot con valor 0.
- **Resultado esperado:** cantidad de cancelados incorporada al tablero.
- **Referencias:** fila CU-08; RN-03/06/09, SEC-08 pendiente, DM-14/15/16/17/18; eventos de Pedidos vía CU-06, sin nueva INT saliente.
- **Estado:** evolución pendiente; PEDIDOS_ENTREGADOS de CU-05 no implementa este análisis.

<a id="cu-09"></a>
## CU-09 — Analizar pagos aprobados y rechazados

- **Actor y objetivo:** Analista vía tablero; contar pagos aprobados y rechazados.
- **Precondiciones:** ninguna.
- **Flujo previsto:** API-04 ampliada llama SRV-07; REP-07 cuenta eventos PAGO_APROBADO y PAGO_RECHAZADO del período; REP-04 actualiza snapshots CANTIDAD_PAGOS_APROBADOS y CANTIDAD_PAGOS_RECHAZADOS en ENT-02 y los incluye en DTO-04/10.
- **Alternativa prevista:** sin eventos de pago, ambos valores 0.
- **Resultado esperado:** ambas métricas disponibles en el tablero.
- **Referencias:** fila CU-09; RN-03/06/09, SEC-09 pendiente, DM-14/15/16/18; eventos de Pagos por CU-06, sin nueva INT saliente.
- **Estado:** evolución pendiente; sin cliente HTTP de Pagos ni eventos persistidos.

<a id="cu-10"></a>
## CU-10 — Analizar tiempos y entregas

- **Actor y objetivo:** Analista vía tablero; contar entregas y calcular su tiempo promedio.
- **Precondiciones:** ninguna.
- **Flujo previsto:** API-04 ampliada llama SRV-08; REP-07 obtiene ENTREGA_COMPLETADA del período; cuenta eventos y promedia valor; REP-04 guarda CANTIDAD_ENTREGAS y TIEMPO_PROMEDIO_ENTREGA, incluidas en DTO-04/10.
- **Alternativa prevista:** sin entregas, cantidad 0 y promedio null. Su persistencia requiere aclaración por el NOT NULL actual de ENT-02.
- **Resultado esperado:** ambas métricas agregadas al tablero.
- **Referencias:** fila CU-10; RN-03/06/09, SEC-10 pendiente, DM-14/15/16/18; eventos de Logística vía CU-06, sin nueva INT saliente.
- **Estado:** evolución pendiente; no existen estos tipos de métricas ni su cálculo.

## Contratos OpenAPI

La [tabla API/CU/estado](api/README.md#contratos-publicados-y-trazabilidad) distingue
implementación y propuesta. Los contratos canónicos son [API-01 a API-04](api/openapi.yaml)
y [API-05/06 propuestas](api/openapi-evolucion-propuesta.yaml). CU-08/09/10
ampliarán API-04 según DM-18. Los cambios incompatibles de errores se describen
en la [documentación API](api/README.md#cambio-incompatible-del-contrato-de-errores).
