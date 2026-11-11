# ADR-003 · Representación de entidades en MariaDB y MongoDB

**Estado:** Aprobado · PR #36 (2026-11-10) · HU-011, HU-017, HU-020, HU-023, HU-026

## Problema

MariaDB utiliza tablas y FK, incluido Estudios con identidad compuesta `(id_prof, cc_per)`; MongoDB usa documentos. Hay que conservar reglas de negocio sin acoplar el dominio a una representación física.

## Alternativas por evaluar

- Referencias entre documentos/colecciones con validaciones en caso de uso.
- Embebido de relaciones documentales cuando encaje con operaciones y cardinalidades.
- Identificadores físicos de Mongo separados del identificador estable del dominio, mediante Mapper.

## Criterios que debe cumplir cualquier decisión

No perder identidad, teléfono dueño ni asociación Estudios–Profesión–Persona; respetar unicidad y borrado acordados; garantizar que CRUD en ambos motores produce semántica de dominio equivalente. No inventar sincronización entre bases.

## Decisión (HU-011)

Se conserva la representación que ya existe en la plantilla. En MariaDB hay tablas con PK y FK. En MongoDB hay una colección por recurso enlazada con `@DocumentReference`, sin subdocumentos embebidos. El `_id` de cada documento es la clave del dominio: `cc`, `id`, `num`, y `"<cc>-<id_prof>"` para Estudios. Las reglas que MongoDB no garantiza por sí mismo (unicidad en el alta, existencia de referencias y borrado restrictivo) las aplica el caso de uso igual en los dos motores. Así ambos tienen la misma semántica sin depender de las FK. Detalle en [persistencia §6.1](../../03-contratos/persistencia.md#61-reglas-comunes--aprobado-hu-011), reglas C-01 a C-05.

**Opciones descartadas:**
- *Embeber teléfonos y estudios en Persona:* obligaría a reescribir los documentos y mappers existentes y haría que Estudios dependiera solo de Persona.
- *Borrado en cascada:* MariaDB no declara `ON DELETE CASCADE` y en MongoDB habría que borrar los dependientes a mano. El borrado restrictivo se comporta igual en ambos motores con una sola comprobación en el caso de uso.

## Decisiones acordadas

Acordadas por el equipo en la revisión del PR #36.

| ID | Pregunta | Decisión |
|---|---|---|
| D-01 | ¿Se admite el género `OTHER`? | No. Solo `M` y `F`, igual en REST y CLI y en ambos motores, para no cambiar el `ENUM('M','F')` del DDL. El mapper REST traduce solo esos dos valores (HU-006); el rechazo de cualquier otro con ERR-01 corresponde a la validación de Persona (HU-012). |
| D-02 | ¿Borrado restrictivo o en cascada? | Restrictivo (C-05). El caso de uso comprueba los dependientes **antes** de llamar a `delete`. Además se quita `REMOVE` de la cascada de `PersonaEntity` y `ProfesionEntity`, para que JPA no borre teléfonos ni estudios y la FK quede como respaldo. |
| D-03 | ¿Qué código HTTP usa una referencia inexistente (ERR-05)? | `422`, solo para las referencias que vienen en el cuerpo. Si no existe el registro que indica la ruta (consultar, editar o eliminar, incluido el par de Estudios), es `404` (ERR-03). |
| D-04 | ¿Se reemplaza `GET /api/v1/persona/{database}` por el parámetro `database` en todas las rutas? | Sí ([API REST §3.1](../../03-contratos/api-rest.md#31-rutas-comunes--aprobado-hu-011)). Los ejemplos de Swagger se actualizan para que coincidan (HU-014 y HU-028). |
| D-05 | ¿Tamaño de página por defecto y máximo? | 10 por defecto y 100 como máximo, con el orden estable de C-06. |

## Consecuencias y verificación

- No se crean colecciones ni índices nuevos: el `_id` de cada documento ya es la clave del dominio.
- Los puertos de salida y los casos de uso de cada recurso implementan C-01 a C-09 (HU-012, HU-016, HU-019 y HU-022), y los adaptadores ajustan la cascada de D-02 (HU-013 y HU-017).
- Las pruebas de relación y de error en ambos motores se registran en HU-026, y la coherencia entre motores y mappers en HU-027.
