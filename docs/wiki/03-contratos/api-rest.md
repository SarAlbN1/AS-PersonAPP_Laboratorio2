# API REST · PersonAPP

**Estado:** contrato objetivo parcialmente especificado; solo dos rutas están identificadas en la plantilla de referencia. No se declara ningún CRUD completo como implementado.  
[← Contratos](README.md) · [SRS](../01-producto/01-SRS.md) · [CLI](cli.md) · [Errores](validaciones-y-errores.md)

## 1. Responsabilidad y frontera

El módulo `rest-input-adapter` recibe HTTP, valida formato de entrada, mapea DTO a tipos de aplicación y delega mediante puertos de entrada/casos de uso. La elección del motor se recibe **en cada petición o por un mecanismo equivalente sin estado global compartido**, pendiente de decisión formal ([ADR-002](../02-arquitectura/adr/ADR-002-seleccion-dinamica.md)). El controlador no accede a JPA, MongoRepository ni a las bases directamente.

**Requisito:** CRUD de Persona, Profesión, Teléfono y Estudios mediante REST en ambos motores (RF-01, RF-02, RF-04 y RF-05). OpenAPI debe describir la **API efectiva**, no una maqueta imaginada.

## 2. Inventario de la plantilla inicial

| Método | Ruta identificada | Para qué | Verificación del fork |
|---|---|---|---|
| `GET` | `/api/v1/persona/{database}` | Listar personas en el motor indicado | HU-005: `MARIA` y `MONGO` devuelven 200 con las cinco personas de cada motor. Un motor desconocido devuelve 200 con `[]` ([evidencia](../04-implementacion/ejecucion-local.md#arranque-de-rest-y-cli-hu-005)). |
| `POST` | `/api/v1/persona` | Crear persona | HU-005: devolvía 500 con `NullPointerException` en `PersonaMapperMaria.validateGenero`. HU-006: con `database` en el cuerpo, como en la plantilla, devuelve 200 y guarda en el motor indicado. La respuesta siempre indica `MariaDB`, y un `sex` o un `dni` inválido devuelve 500 ([evidencia](../04-implementacion/ejecucion-local.md#corrección-del-mapper-rest-hu-006)). El contrato aprobado (§3.1: `?database=`, `201` y ERR-01) llega con HU-014. |

**DTO inicial referenciado:** `PersonaRequest` con `dni`, `firstName`, `lastName`, `age`, `sex`, `database`. Tipos, serialización, campos obligatorios y reglas de validación deben leerse del código del fork. La referencia del profesor sitúa Swagger UI en `http://localhost:3000/swagger-ui.html` con configuración original; no se garantiza la misma ruta tras migrar.

Una operación declarada en `PersonInputPort` o `PersonUseCase` no puede contarse aquí como ruta HTTP hasta que aparezca en el controlador y en OpenAPI.

## 3. Matriz de recursos y operaciones objetivo

La siguiente tabla **es un inventario de capacidades a diseñar**, no una lista de rutas ya existentes. Mantiene la nomenclatura REST convencional, pero no fija unilateralmente la versión, pluralización, idioma o ubicación de `database`.

| Recurso | Crear | Listar todos | Listar parcial | Editar | Eliminar | Agregación (contar) | HU |
|---|---|---|---|---|---|---|---|
| Persona | POST | GET | GET | PUT | DELETE | GET | HU-014 |
| Profesión | POST | GET | GET | PUT | DELETE | GET | HU-018 |
| Teléfono | POST | GET | GET | PUT | DELETE | GET | HU-021 |
| Estudios | POST | GET | GET | PUT | DELETE | GET | HU-024 |

**Contrato propuesto para las lecturas.** Listar todos devuelve la colección completa del motor elegido. Listar parcial devuelve una página con tamaño positivo y orden estable por clave, incluida la clave compuesta de Estudios. Una página fuera del rango devuelve una colección vacía. Los parámetros inválidos producen un error de validación. Contar devuelve un entero no negativo con el total de la colección en el motor elegido, independientemente de la página. Una colección vacía devuelve 0.

Editar y eliminar identifican el registro por su clave. La edición conserva la identidad y una operación sobre una clave ausente devuelve el error documentado. El detalle por clave puede seguir siendo un método interno del caso de uso.

**Criterios para fijar las rutas definitivas:** coherencia con `/api/v1/persona`, compatibilidad con consumidores de la base, identificadores (`cc`, `id`, `num`, par `id_prof`/`cc_per`), URL del recurso retornado y selector dinámico de motor. El equipo debe decidir si conserva la ruta actual o introduce una v2/versionado explícito, y registrar esa decisión **antes** de consumir el contrato desde pruebas.

### 3.1 Rutas comunes · Aprobado (HU-011)

Estas rutas se aprobaron en HU-011 (#36). No existen hasta que las implementen HU-014, HU-018, HU-021 y HU-024. Las reglas de identidad, orden, paginación, conteo y motor son las de [persistencia §6.1](persistencia.md#61-reglas-comunes--aprobado-hu-011).

`{recurso}` es `persona`, `profesion`, `telefono` o `estudio`, en singular, como la ruta actual `/api/v1/persona`. La clave es `{cc}`, `{id}` o `{num}`; en Estudios son dos segmentos: `/{idProfesion}/{ccPersona}`. Todas las rutas exigen el parámetro de consulta `database=MARIA|MONGO`.

| Operación | Método y ruta | Éxito | Errores |
|---|---|---|---|
| Crear | `POST /api/v1/{recurso}?database=` | `201` con el registro creado | ERR-01, ERR-02, ERR-04, ERR-05, ERR-07 |
| Listar todos | `GET /api/v1/{recurso}?database=` | `200` con un arreglo ordenado por clave | ERR-02, ERR-07 |
| Listar parcial | `GET /api/v1/{recurso}/pagina?database=&page=0&size=10` | `200` con un arreglo; vacío si la página está fuera de rango | ERR-01, ERR-02, ERR-07 |
| Editar | `PUT /api/v1/{recurso}/{clave}?database=` | `200` con el registro actualizado | ERR-01, ERR-02, ERR-03, ERR-05, ERR-07, ERR-09 |
| Eliminar | `DELETE /api/v1/{recurso}/{clave}?database=` | `204` sin cuerpo | ERR-02, ERR-03, ERR-06, ERR-07 |
| Contar | `GET /api/v1/{recurso}/conteo?database=` | `200` con un entero (por ejemplo, `5`) | ERR-02, ERR-07 |

`page` y `size` son opcionales y valen 0 y 10 por defecto. Los errores usan el [formato común](validaciones-y-errores.md#22-formato-de-error--aprobado-hu-011).

**DTO propuestos.** Persona conserva `PersonaRequest` (`dni`, `firstName`, `lastName`, `age`, `sex`) sin el campo `database`, que pasa al parámetro de consulta. Para los demás recursos se usan los mismos nombres que el dominio:
- `ProfesionRequest`: `id`, `name`, `description`.
- `TelefonoRequest`: `number`, `company`, `ownerId`.
- `EstudioRequest`: `professionId`, `personId`, `graduationDate`, `universityName`.

Las respuestas devuelven esos mismos campos. Los DTO usan tipos JSON reales (número, texto o fecha) en lugar de convertir todo a `String`.

**Cambios frente a la plantilla (decisión D-04):** la ruta `GET /api/v1/persona/{database}` se sustituye por `GET /api/v1/persona?database=`. El campo `database` del cuerpo del `POST` pasa a ser parámetro de consulta. Así las seis operaciones eligen el motor del mismo modo, y `/{clave}` queda libre para editar y eliminar. Los ejemplos de Swagger se actualizan para usar `?database=` (HU-014 y HU-028).

## 4. Ficha obligatoria por endpoint

Usar esta tabla para **cada una de las operaciones reales** al desarrollarla:

| Campo | Información que debe registrarse |
|---|---|
| Identificación | HU, recurso, operación, versión y estado (`propuesto`, `implementado`, `verificado`) |
| HTTP | Método y ruta **exacta**, incluyendo claves y parámetro/cabecera de motor |
| Entrada | Path, query, headers, JSON, obligatoriedad, tipos, ejemplo válido |
| Salida | Código de éxito, JSON o ausencia de cuerpo, cabeceras y ejemplo |
| Fallos | Entrada inválida, recurso no hallado, duplicado, FK/relación inválida, motor no admitido y base caída |
| Persistencia | Adaptador MariaDB/MongoDB elegido, sin replicación implícita |
| Pruebas | ID del caso, evidencia, commit/PR y contraste con Swagger |

**Plantilla de entrada para una futura ruta (NO implementada):**

```text
ID: REST-<recurso>-<operacion>
Estado: Propuesto
Método + ruta: POR DECIDIR
Motor: MARIA / MONGO (mecanismo POR DECIDIR)
Solicitud: POR DEFINIR (tipos y ejemplo)
Respuesta exitosa: POR DEFINIR
Errores: consultar validaciones-y-errores.md
HU: HU-014 / HU-018 / HU-021 / HU-024
Prueba y evidencia: PENDIENTES
```

## 5. Identidad y representación

| Recurso | Identidad base | Precaución |
|---|---|---|
| Persona | `cc` (MariaDB) / `dni` en `PersonaRequest` | Acordar mapeo y tipos; no perder identificador |
| Profesión | `id` | Definir representación concreta en DTO y MongoDB |
| Teléfono | `num` | Tratar como identificador textual si así figura en esquema; conservar ceros iniciales |
| Estudios | Combinación `id_prof`, `cc_per` en MariaDB | No introducir un ID nuevo sin ADR y revisión de contratos |

No inventar nombres de DTO para los tres recursos pendientes ni afirmar que el documento Mongo refleja relaciones SQL idénticas. Revisar [persistencia](persistencia.md).

## 6. Respuestas HTTP: convención propuesta, no implementación certificada

| Situación | Candidato de respuesta | Confirmación requerida |
|---|---|---|
| Listado/consulta | `200 OK` | Confirmar esquema de salida y lista vacía |
| Creación | `201 Created` | Confirmar URL en `Location` y esquema de respuesta |
| Actualización sin cuerpo | `204 No Content` | Confirmar método y política de respuesta |
| Eliminación | `204 No Content` | Confirmar si existe o no cuerpo |
| Petición incorrecta, motor inválido | `400 Bad Request` | Acordar formato de error y reglas |
| Entidad no encontrada | `404 Not Found` | Acordar comportamiento de update/delete |
| Identificador duplicado o conflicto de relación | `409 Conflict` | Acordar manejo de integridad en ambos motores |
| Almacén caído / fallo inesperado | `503` o `500` según causa | Normalizar y evitar filtrar trazas |

Esta política debe aprobarse por equipo en HU-011 e implementarse de forma uniforme. Un `409` no se garantiza solo porque sea razonable.

## 7. Swagger / OpenAPI

- La documentación la genera o publica el módulo REST con herramientas compatibles con la versión de Spring Boot de la entrega (HU-028).
- Swagger debe mostrar únicamente endpoints reales; los ejemplos deben concordar con DTO y reglas implementados.
- En la revisión final comparar método, ruta, query/path, objeto requerido, respuestas y códigos con peticiones ejecutadas.
- Registrar versión exacta de OpenAPI y dependencia en [`versiones.md`](../04-implementacion/versiones.md), cuando esté resuelta.

## 8. Verificación de selección de motor

Con REST ejecutándose una sola vez, emitir **tres solicitudes consecutivas** al mismo tipo de operación: `MARIA → MONGO → MARIA`. Comprobar en las fuentes físicas correspondientes qué consulta se ejecutó, sin asumir sincronización entre bases. Repetir con una escritura y lecturas donde sea posible. Si un motor cae, no redirigir silenciosamente al otro. Ver HU-025 y [backlog único](../06-backlog/README.md).

## 9. Registro de cierre

**Pendiente:** URL del fork, commit, OpenAPI real exportado, endpoints efectivos, pruebas para los cuatro recursos, resultado de la alternancia y evidencia de Swagger. Vincular al PR de cada HU y a HU-028.

## 10. Comportamiento existente inspeccionado, sin prueba HTTP

Los seis campos de `PersonaRequest` son String, sin anotaciones Bean Validation. `PersonaResponse` los hereda y agrega `status`. No hay `@ResponseStatus(CREATED)` ni `ResponseEntity` en el POST: 201/Location siguen siendo propuestas, no contrato actual. GET captura motor inválido y retorna lista vacía; POST lo captura y retorna null. Un selector null no se maneja explícitamente. No se verificaron códigos HTTP por ejecución.

El mapper de petición retorna un Person vacío; POST no demuestra alta funcional. La respuesta de POST se construye siempre con etiqueta MariaDB aun si se selecciona MongoDB. El selector existente está en un campo mutable del adaptador REST; el aislamiento propuesto no está demostrado. Las únicas rutas declaradas son las dos del inventario.
