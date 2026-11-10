# ADR-003 · Representación de entidades en MariaDB y MongoDB

**Estado:** Propuesto en HU-011 · HU-011, HU-017, HU-020, HU-023, HU-026

## Problema

MariaDB utiliza tablas y FK, incluido Estudios con identidad compuesta `(id_prof, cc_per)`; MongoDB usa documentos. Hay que conservar reglas de negocio sin acoplar el dominio a una representación física.

## Alternativas por evaluar

- Referencias entre documentos/colecciones con validaciones en caso de uso.
- Embebido de relaciones documentales cuando encaje con operaciones y cardinalidades.
- Identificadores físicos de Mongo separados del identificador estable del dominio, mediante Mapper.

## Criterios que debe cumplir cualquier decisión

No perder identidad, teléfono dueño ni asociación Estudios–Profesión–Persona; respetar unicidad y borrado acordados; garantizar que CRUD en ambos motores produce semántica de dominio equivalente. No inventar sincronización entre bases.

## Decisión propuesta (HU-011)

Se conserva la representación que ya existe en la plantilla. En MariaDB hay tablas con PK y FK. En MongoDB hay una colección por recurso enlazada con `@DocumentReference`, sin subdocumentos embebidos. El `_id` de cada documento es la clave del dominio: `cc`, `id`, `num`, y `"<cc>-<id_prof>"` para Estudios. Las reglas que MongoDB no garantiza por sí mismo (unicidad en el alta, existencia de referencias y borrado restrictivo) las aplica el caso de uso igual en los dos motores. Así ambos tienen la misma semántica sin depender de las FK. Detalle en [persistencia §6.1](../../03-contratos/persistencia.md#61-reglas-comunes--propuesto-hu-011), reglas C-01 a C-05.

**Opciones descartadas:**
- *Embeber teléfonos y estudios en Persona:* obligaría a reescribir los documentos y mappers existentes y haría que Estudios dependiera solo de Persona.
- *Borrado en cascada:* MariaDB no declara `ON DELETE CASCADE` y en MongoDB habría que borrar los dependientes a mano. El borrado restrictivo se comporta igual en ambos motores sin código adicional de borrado.

## Decisiones abiertas para el equipo

| ID | Pregunta | Propuesta |
|---|---|---|
| D-01 | ¿Se admite el género `OTHER`? | No. Se rechaza en la entrada para no cambiar el `ENUM('M','F')` del DDL. |
| D-02 | ¿Borrado restrictivo o en cascada? | Restrictivo (C-05). |
| D-03 | ¿Qué código HTTP usa una referencia inexistente (ERR-05)? | `422`. |
| D-04 | ¿Se reemplaza `GET /api/v1/persona/{database}` por el parámetro `database` en todas las rutas? | Sí ([API REST §3.1](../../03-contratos/api-rest.md#31-rutas-comunes--propuesto-hu-011)). |
| D-05 | ¿Tamaño de página por defecto y máximo? | 10 y 100. |

## Pendiente antes de aprobar

Leer scripts y repositorios reales, decidir estructura de colecciones/índices, documentar comportamiento de integridad y preparar pruebas de relación y errores. Actualizar [persistencia](../../03-contratos/persistencia.md) y el SDD en el mismo PR.
