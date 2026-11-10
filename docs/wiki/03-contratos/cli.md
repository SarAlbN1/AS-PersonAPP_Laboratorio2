# Contrato funcional de la CLI · PersonAPP

**Estado:** especificación objetivo basada en el laboratorio; menú definitivo y mensajes sujetos a ejecución.  
[← Contratos](README.md) · [API REST](api-rest.md) · [Validaciones](validaciones-y-errores.md)

## 1. Propósito y responsabilidades

El `cli-input-adapter` recibe acciones de terminal, construye solicitudes al puerto de entrada y presenta resultados comprensibles. No contiene SQL, consultas MongoDB ni reglas de negocio duplicadas. El modelo de dominio y los casos de uso deben poder atender tanto REST como CLI mediante las mismas abstracciones.

**Base documentada:** `PersonaMenu` permite elegir MariaDB/MongoDB y listar personas. Las demás opciones de la plantilla aparecen incompletas. No se consideran implementadas por existir en pantalla.

## 2. Contrato funcional objetivo

| Recurso | Crear | Listar todos | Listar parcial | Editar | Eliminar | Contar | HU |
|---|---|---|---|---|---|---|---|
| Persona | Sí | Sí | Sí | Sí | Sí | Sí | HU-015 |
| Profesión | Sí | Sí | Sí | Sí | Sí | Sí | HU-018 |
| Teléfono | Sí | Sí | Sí | Sí | Sí | Sí | HU-021 |
| Estudios | Sí | Sí | Sí | Sí | Sí | Sí | HU-024 |

La HU-026 verifica las operaciones por recurso y motor. [Backlog único](../06-backlog/README.md).

## 3. Flujos propuestos de interacción

La numeración de opciones y textos son **orientativos**; no deben copiarse como comportamiento existente antes de inspeccionar el menú real.

```text
INICIO
  → seleccionar motor (MARIA | MONGO)
  → seleccionar recurso (Persona | Profesión | Teléfono | Estudios)
  → seleccionar operación (crear | listar todos | listar parcial | editar | eliminar | contar)
  → introducir datos requeridos
  → invocar caso de uso mediante puerto de entrada
  → mostrar resultado o error controlado
  → volver al menú sin reiniciar
```

### Selección dinámica obligatoria

1. Iniciar la CLI una sola vez.
2. Seleccionar `MARIA`, consultar/crear y comprobar el dato correspondiente.
3. Cambiar a `MONGO` desde el menú, sin reiniciar proceso, y repetir.
4. Volver a `MARIA` en la misma sesión y comprobar que recupera su propio estado.
5. Introducir selector inválido: mostrar mensaje útil y permitir corrección.

**Regla:** el selector de la CLI no debe modificar una variable global que afecte solicitudes REST concurrentes. La solución concreta se formaliza en [ADR-002](../02-arquitectura/adr/ADR-002-seleccion-dinamica.md).

### Menú común · Propuesto (HU-011)

Conserva el orden de la plantilla (primero recurso, luego motor) y aplica las reglas de [persistencia §6.1](persistencia.md#61-reglas-comunes--propuesto-hu-011).

```text
Menú principal:  1 Persona · 2 Profesión · 3 Teléfono · 4 Estudios · 0 Salir
Menú de motor:   1 MariaDB · 2 MongoDB · 0 Regresar
Menú de operaciones (igual para los cuatro recursos):
  1 Crear · 2 Listar todos · 3 Listar parcial · 4 Editar · 5 Eliminar · 6 Contar · 0 Regresar al menú de motor
```

- **Cambio de motor:** con 0 se vuelve al menú de motor y se elige el otro sin reiniciar la aplicación.
- **Listar parcial:** pide la página (desde 0) y el tamaño (1 a 100). Muestra los registros y una línea `Página N · M registros`.
- **Editar y eliminar:** piden primero la clave (en Estudios, la profesión y luego la persona). Al editar, muestra el registro actual antes de pedir los campos editables.
- **Contar:** muestra `Total en <motor>: N`.
- **Errores:** siguen el [formato común](validaciones-y-errores.md#22-formato-de-error--propuesto-hu-011) (`[ERR-xx] mensaje`). Una entrada no numérica se descarta y se vuelve a pedir, sin cerrar la sesión.

## 4. Entradas esperadas

| Recurso | Identidad base | Datos relevantes del esquema de referencia | Validación mínima objetivo |
|---|---|---|---|
| Persona | `cc` | nombre, apellido, género, edad | identidad válida, campos obligatorios, género admitido, edad según regla aprobada |
| Profesión | `id` | `nom`, `des` | identidad válida, nombre obligatorio |
| Teléfono | `num` | `oper`, `duenio` | número textual válido y dueño existente |
| Estudios | `id_prof`, `cc_per` | `fecha`, `univer` | ambas referencias existentes y par no duplicado |

Los tipos y límites exactos se fijan en [persistencia](persistencia.md) y [validaciones](validaciones-y-errores.md) al contrastar DDL, dominio y DTO.

## 5. Contrato de resultados

| Resultado | Comportamiento deseado de CLI | Prohibido |
|---|---|---|
| Operación correcta | Mostrar confirmación/registro e identidad | Mostrar éxito antes de persistir |
| Listado vacío | Mensaje explícito, retorno normal al menú | Presentar excepción como resultado normal |
| No encontrado | Informar el identificador consultado sin secretos | `NullPointerException` al usuario |
| Duplicado / relación inválida | Explicar causa funcional y pedir corrección | Mostrar stacktrace o SQL crudo |
| Valor de motor no reconocido | Solicitar selección válida, sin escribir | Elegir otra BD silenciosamente |
| Motor no disponible | Comunicar falla, conservar datos y permitir reintento | Pasar al otro motor sin autorización |
| Entrada con tipo incorrecto | Solicitar un nuevo dato, sin cerrar la sesión | Bloquear menú o registrar datos parciales |

## 6. Casos de prueba para HU-026

- Para cada recurso: alta → listado total → listado parcial → edición → conteo → eliminación → verificar ausencia en listado y decremento del conteo.
- Repetir la secuencia en `MARIA` y `MONGO` sin cambiar código.
- Intentar crear registros duplicados o relaciones inexistentes; comprobar que el almacén no se altera.
- Comprobar que `num` no pierde ceros iniciales, si el contrato lo define como texto.
- Alternar motor en la misma sesión, con una prueba que distinga físicamente ambos almacenes.
- Registrar entrada de terminal, salida real, fecha, versión y evidencia; las capturas sin datos legibles no sustituyen la prueba.

## 7. Ficha de opción implementada

```text
ID: CLI-<recurso>-<operacion>
Estado: Propuesto / Implementado / Verificado
Menú/ruta de opciones real:
Entradas exigidas y validación:
Operación del puerto de entrada:
Salida de éxito:
Salida(s) de error:
Motor probado: MARIA / MONGO
Prueba, commit, PR:
```

**Pendientes de confirmar:** árbol exacto de `PersonaMenu`, DTO y Mapper CLI reales, política de errores, navegación de vuelta y comportamiento de cierre. Cerrar estas decisiones antes de declarar el contrato final.

## 8. Árbol real por inspección

`MenuPrincipal`: 0 salir; 1 Persona; 2 Profesión; 3 Teléfono; 4 Estudios. Opciones 2–4 solo registran `Implementar Menu`. Persona: 1 MariaDB, 2 MongoDB, 0 regresar. Luego: 1 listar personas, 0 regresar. El flujo original elige primero recurso y después motor; el diagrama de la sección 3 es una propuesta. No existen formularios de alta/edición ni búsqueda/eliminación en el menú. `PersonaModelCli` contiene cc Integer, nombre/apellido/genero String y edad Integer; `PersonaMapperCli` realiza la conversión de salida. Entrada no numérica requiere revisión porque la recursión de lectura no consume el token inválido.
