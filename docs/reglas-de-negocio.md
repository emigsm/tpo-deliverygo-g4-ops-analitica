# Reglas de negocio

Definiciones preservadas del [TPO corregido y evolución](referencias/README.md).
La última columna distingue aplicación actual y diseño futuro.

| ID | Definición | CU relacionados | Validación y estado |
| --- | --- | --- | --- |
| RN-01 | Solo puede calificarse un pedido que haya sido entregado. | CU-01, CU-02 | SRV-01 consulta INT-01; implementada con mock |
| RN-02 | Una reseña por usuario, pedido y tipo de objetivo (ver DM-02). | CU-01, CU-02 | REP-01 y UNIQUE de ENT-01; implementada |
| RN-03 | Las métricas deben indicar período y fuente. | CU-05; evolución CU-08, CU-09, CU-10 | ENT-02 NOT NULL y SRV-03; evolución pendiente |
| RN-04 | La puntuación de una reseña debe estar entre 1 y 5. | CU-01, CU-02 | SRV-01; discrepancia sobre CHECK en arquitectura |
| RN-05 | Una reseña es inmutable una vez creada (sin edición o borrado). | CU-01, CU-02 | Sin PUT/DELETE, columnas no actualizables; implementada |
| RN-06 | No puede haber dos snapshots de “Métrica” para la misma combinación de fuente, tipo de métrica y período. | CU-05; evolución CU-08, CU-09, CU-10 | UNIQUE de ENT-02 y actualización por REP-05; evolución pendiente |
| RN-07 | El pedido debe pertenecer al usuario autenticado. | CU-01, CU-02 | SRV-01 compara PedidoDTO.usuarioId con header temporal; JWT pendiente |
| RN-08 | El usuario, comercio o repartidor referenciados deben existir y estar activos. | CU-01, CU-02 | SRV-01 con INT-02/03/04; implementada con mocks |
| RN-09 | Todo evento analítico debe indicar tipoEvento, moduloOrigen, entidadId y fechaEvento. | CU-06 | SRV-04 / ENT-03 previstos; no implementada |

## Matriz RN y CU

| CU | RN |
| --- | --- |
| CU-01, CU-02 | RN-01, RN-02, RN-04, RN-05, RN-07, RN-08 |
| CU-03, CU-04 | No aplica: consulta de solo lectura |
| CU-05 | RN-03, RN-06 |
| CU-06 | RN-09 |
| CU-07 | No aplica según ficha funcional |
| CU-08, CU-09, CU-10 | RN-03, RN-06, RN-09 |

La evolución vincula RN-09 directamente a CU-06 en su tabla de reglas y también
la incluye en CU-08/09/10 en su matriz: estos consumen eventos previamente
validados. Se conservan ambas referencias. REP-08 menciona RN-03 en la evolución,
aunque la ficha de CU-07 indica “No aplica”; no se cambia esa definición.

Véanse [trazabilidad completa](casos-de-uso.md#trazabilidad) y
[discrepancias de implementación](arquitectura.md#discrepancias).
