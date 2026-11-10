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

### 2.1 Tipos y límites · Aprobado (HU-011)

Los límites salen del DDL de MariaDB (`scripts/persona_ddl_maria.sql`) y de los modelos de `domain`. MongoDB aplica los mismos límites desde el caso de uso. Una violación produce ERR-01, salvo que se indique otra cosa.

| Recurso | Campo (dominio · MariaDB) | Tipo | Regla propuesta |
|---|---|---|---|
| Persona | `identification` · `cc` | Entero | Obligatorio, entre 1 y 2147483647. |
| Persona | `firstName` · `nombre`, `lastName` · `apellido` | Texto | Obligatorios, de 1 a 45 caracteres tras recortar espacios. |
| Persona | `gender` · `genero` | `MALE` o `FEMALE` | Obligatorio. `OTHER` existe en `Gender`, pero el `ENUM('M','F')` de MariaDB no lo admite; se rechaza con ERR-01 (decisión D-01). El mapper REST traduce solo `M` y `F`; el rechazo de los demás valores corresponde a la validación de Persona. |
| Persona | `age` · `edad` | Entero | Opcional. Si viene, `≥ 0`. Sin máximo, para no inventar un límite sin fuente. `isValidAge` debe aceptar `null`. |
| Profesión | `identification` · `id` | Entero | Obligatorio, entre 1 y 2147483647. |
| Profesión | `name` · `nom` | Texto | Obligatorio, de 1 a 90 caracteres. |
| Profesión | `description` · `des` | Texto | Opcional, sin límite (`TEXT`). |
| Teléfono | `number` · `num` | Texto | Obligatorio, de 1 a 15 dígitos (`^[0-9]{1,15}$`). Se guarda como texto para conservar los ceros iniciales. |
| Teléfono | `company` · `oper` | Texto | Obligatorio, de 1 a 45 caracteres. |
| Teléfono | `owner` · `duenio` | Entero (`cc`) | Obligatorio. La persona debe existir en el motor elegido; si no, ERR-05. |
| Estudios | `profession` · `id_prof`, `person` · `cc_per` | Enteros | Obligatorios. Forman la clave y ambos deben existir en el motor elegido; si no, ERR-05. |
| Estudios | `graduationDate` · `fecha` | Fecha ISO `yyyy-MM-dd` | Opcional. Si viene, debe ser una fecha válida. |
| Estudios | `universityName` · `univer` | Texto | Opcional, hasta 50 caracteres. |

### 2.2 Formato de error · Aprobado (HU-011)

REST responde los errores con el código HTTP de la sección 3 y este cuerpo JSON:

```json
{ "error": "ERR-04", "message": "Ya existe una persona con cc 123456789 en MARIA" }
```

CLI muestra el mismo `message` precedido del código (`[ERR-04] ...`) y vuelve al menú sin cerrar la sesión. Ninguna de las dos expone trazas, SQL ni credenciales.

## 3. Catálogo de condiciones de error

| ID | Condición | Resultado funcional esperado | REST propuesto* | CLI esperado |
|---|---|---|---|---|
| ERR-01 | Cuerpo/tipo/formato inválido | Rechazar sin persistir | `400` | Solicitar corrección |
| ERR-02 | Motor desconocido | Rechazar, no elegir por defecto | `400` | Mostrar opciones admitidas |
| ERR-03 | Entidad consultada no existe | Distinguir ausencia de error técnico | `404` | No encontrado |
| ERR-04 | Clave duplicada | Rechazar alta; mantener integridad | `409` | Registro duplicado |
| ERR-05 | Referencia de dueño/profesión/persona inexistente | Rechazar asociación | `422` (decisión D-03), solo para referencias del cuerpo; si no existe el registro de la ruta, ERR-03 | Referencia inexistente |
| ERR-06 | Borrado con dependientes | Rechazar el borrado (política restrictiva C-05) | `409` | Informar conflicto e indicar los dependientes |
| ERR-07 | MariaDB/MongoDB fuera de servicio | No responder éxito ni saltar al otro motor | `503` | Motor no disponible |
| ERR-08 | Error de mapper / inconsistencia | Registrar diagnóstico, no persistir datos erróneos | `500` si no es entrada inválida | Operación fallida |
| ERR-09 | Ruta y cuerpo con identidad distinta | Rechazar modificación ambigua | `400` | Confirmar clave correcta |
| ERR-10 | Excepción no clasificada | Error genérico y registro técnico seguro | `500` | Error controlado |

*Los códigos REST son la **política acordada** en HU-011 (#36), no respuestas observadas. ERR-05 usa `422` porque la petición está bien formada y la clave propia no choca con nada (no es un `409`), pero apunta a un registro que no existe en el motor elegido; cerrar al definir el contrato HTTP y probarlo sobre ambas persistencias. No hay autorización para afirmar equivalencia de errores simplemente porque sean similares.

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

- **HU-011:** cerrada en #36. Tipos, campos obligatorios y longitudes en §2.1, formato de error en §2.2 y política de borrado en [ADR-003](../02-arquitectura/adr/ADR-003-modelo-persistencia.md#decisiones-acordadas).
- **HU-014/018/021/024:** JSON de petición/respuesta y errores efectivos por endpoint.
- **HU-025:** selector de motor erróneo y aislamiento por sesión/petición.
- **HU-026/027:** batería de casos positivos y negativos, coherencia entre motores y mappers.
- **HU-028:** OpenAPI actualizado con ejemplos y códigos constatados.

## 8. Defectos observados y reglas aún no implementadas

El mapper REST de entrada devuelve un Person vacío; ambos mappers de Estudios devuelven null después de construir Study. Son hallazgos de código, no fallos reproducidos ni prueba de cuál mencionó el docente. `Person.isValidAge` no acepta null con seguridad; OTHER existe en dominio pero SQL admite solo M/F. `create` delega en save sin rechazo explícito de duplicados; edit no comprueba igualdad de claves; Mongo save devuelve la entrada al capturar MongoWriteException. No existe un manejador global HTTP en las clases revisadas. El catálogo anterior es política objetivo, no comportamiento actual certificado.
