# Validaciones y tratamiento de errores · PersonAPP

**Estado:** reglas transversales objetivo, sujetas a tipos reales de dominio, DDL y decisión del equipo en HU-011.  
[← Contratos](README.md) · [Persistencia](persistencia.md) · [REST](api-rest.md) · [CLI](cli.md)

## 1. Separación de responsabilidades

| Lugar | Qué valida o hace | Qué no debe hacer |
|---|---|---|
| REST/CLI (adaptador de entrada) | Sintaxis, tipos, formato, selección de motor, presencia de datos | Inventar reglas diferentes del dominio |
| Aplicación / casos de uso | Precondiciones, existencia, coherencia de operación, mapeo de errores de negocio | Lanzar HTTP desde el dominio |
| Dominio | Invariantes independientes del motor | Depender de Spring, JPA, MongoDB o consola |
| Adaptador de salida | Restricciones de almacenamiento, excepciones técnicas, concurrencia | Filtrar excepciones SQL al cliente |
| Base física | Índices y restricciones garantizables según motor | Sustituir por completo las reglas de negocio |

## 2. Validaciones por recurso

| Recurso | Criterios de validación objetivo | Pendiente antes de codificar |
|---|---|---|
| Persona | Identificador no vacío/válido; nombre/apellido presentes; `genero` conforme a valores permitidos; edad dentro de reglas acordadas | Tipo exacto de `cc`/`dni`, rango, obligatoriedad, longitud |
| Profesión | `id` único y válido; `nom` no vacío; longitud/descripción conforme al esquema | Tipos y límites efectivos |
| Teléfono | `num` único y no vacío, `oper` permitido, `duenio` existente | Longitudes, formato, tratamiento de prefijos y ceros iniciales |
| Estudios | Identidad compuesta no duplicada; persona y profesión existentes; `fecha` válida; `univer` conforme al esquema | Valores nulos, límites y formato de fecha |

**Principio:** no convertir una validación razonable (por ejemplo, edad máxima arbitraria) en requisito docente sin respaldo. Los límites exactos se derivan de fuentes y se aprueban en HU-011.

## 3. Catálogo de condiciones de error

| ID | Condición | Resultado funcional esperado | REST propuesto* | CLI esperado |
|---|---|---|---|---|
| ERR-01 | Cuerpo/tipo/formato inválido | Rechazar sin persistir | `400` | Solicitar corrección |
| ERR-02 | Motor desconocido | Rechazar, no elegir por defecto | `400` | Mostrar opciones admitidas |
| ERR-03 | Entidad consultada no existe | Distinguir ausencia de error técnico | `404` | No encontrado |
| ERR-04 | Clave duplicada | Rechazar alta; mantener integridad | `409` | Registro duplicado |
| ERR-05 | Referencia de dueño/profesión/persona inexistente | Rechazar asociación | `400` o `409` según política | Referencia inexistente |
| ERR-06 | Borrado con dependientes | Aplicar política de integridad acordada | `409` si borrado es restrictivo | Informar conflicto |
| ERR-07 | MariaDB/MongoDB fuera de servicio | No responder éxito ni saltar al otro motor | `503` candidato | Motor no disponible |
| ERR-08 | Error de mapper / inconsistencia | Registrar diagnóstico, no persistir datos erróneos | `500` si no es entrada inválida | Operación fallida |
| ERR-09 | Ruta y cuerpo con identidad distinta | Rechazar modificación ambigua | `400` | Confirmar clave correcta |
| ERR-10 | Excepción no clasificada | Error genérico y registro técnico seguro | `500` | Error controlado |

*Los códigos REST son **propuesta de política**, no respuestas observadas; cerrar al definir el contrato HTTP y probarlo sobre ambas persistencias. No hay autorización para afirmar equivalencia de errores simplemente porque sean similares.

## 4. Política mínima para errores técnicos

1. Registrar `tipo`, recurso, motor solicitado, operación y un identificador de solicitud si el proyecto dispone de él.
2. No registrar contraseñas, conexiones completas con credenciales, datos personales innecesarios ni stacktraces en respuestas públicas.
3. Traducir excepciones de base al error funcional cuando sea seguro y verificable; mantener `500` para fallos inesperados.
4. Proteger las operaciones: no mostrar éxito antes de que termine escritura; no realizar fallback silencioso a otra base.
5. Aplicar la misma semántica del dominio a ambos adaptadores y ambas interfaces, aunque el mecanismo físico de integridad sea diferente.

## 5. Mapper: riesgo conocido del laboratorio (HU-006)

El profesor advirtió un error en un Mapper de la plantilla, **sin identificar cuál ni la causa**. No atribuir la advertencia docente a una clase concreta sin fuente. La inspección estática sí permite identificar defectos independientes, aunque su reproducción siga pendiente.

**Protocolo de diagnóstico:** ejecutar la versión original → reproducir entrada que falla → guardar traza/error → identificar transformación defectuosa → escribir prueba que falle antes de corregir → corregir → volver a ejecutar y comprobar ida/vuelta en ambos adaptadores pertinentes.

| Campo de evidencia | Valor |
|---|---|
| Archivo y método afectado | Pendiente |
| Solicitud y salida anteriores | Pendiente |
| Causa raíz | Pendiente |
| Prueba de regresión | Pendiente |
| Commit/PR | Pendiente |

## 6. Pruebas negativas obligatorias

- Registro duplicado por cada recurso, en cada motor.
- Consulta/edición/eliminación de identidad inexistente.
- Teléfono con dueño inexistente y estudios con persona o profesión inexistente.
- Operación sobre motor no reconocido o no disponible, por REST y CLI.
- Diferencia de identidad de URL/cuerpo cuando aplique.
- Entrada vacía o con tipos incompatibles y estructura/fechas inválidas.
- Integridad después de un fallo: confirmar que no hubo escrituras parciales no previstas.

El caso concreto y su ejecución se registran en [backlog único](../06-backlog/README.md), HU-026 y el PR. Los ejemplos hipotéticos no equivalen a evidencia real.

## 7. Decisiones abiertas y cierre

- **HU-011:** tipos, campos obligatorios, longitudes, políticas de borrado y traducción de error.
- **HU-014/018/021/024:** JSON de petición/respuesta y errores efectivos por endpoint.
- **HU-025:** selector de motor erróneo y aislamiento por sesión/petición.
- **HU-026/027:** batería de casos positivos y negativos, coherencia entre motores y mappers.
- **HU-028:** OpenAPI actualizado con ejemplos y códigos constatados.

## 8. Defectos observados y reglas aún no implementadas

El mapper REST de entrada devuelve un Person vacío; ambos mappers de Estudios devuelven null después de construir Study. Son hallazgos de código, no fallos reproducidos ni prueba de cuál mencionó el docente. `Person.isValidAge` no acepta null con seguridad; OTHER existe en dominio pero SQL admite solo M/F. `create` delega en save sin rechazo explícito de duplicados; edit no comprueba igualdad de claves; Mongo save devuelve la entrada al capturar MongoWriteException. No existe un manejador global HTTP en las clases revisadas. El catálogo anterior es política objetivo, no comportamiento actual certificado.
