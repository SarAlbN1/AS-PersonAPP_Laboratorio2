# Lista de entrega del Laboratorio 2

Cada casilla marcada cita su evidencia. Solo se marca lo comprobado sobre `main`; lo demás queda pendiente.

- [x] Fork propio del repositorio del profesor, URL registrada: <https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2> ([README](../../../README.md#repositorios-y-colaboración), HU-001, PR #30).
- [x] REST y CLI compilados y ejecutados ([arranque de la plantilla](../04-implementacion/ejecucion-local.md#arranque-de-rest-y-cli-hu-005), [migración ejecutada](../04-implementacion/versiones.md#migración-ejecutada-hu-007) y [verificación final](../04-implementacion/pruebas/README.md#verificación-final-de-las-seis-operaciones-hu-026)).
- [x] Persona, Profesión, Teléfono y Estudios con consulta por identificador, listado completo, conteo, alta, edición y eliminación en REST y CLI, más el listado parcial como operación adicional: HU-026 dio 162 de 162 comprobaciones por REST y 123 de 123 por CLI ([pruebas](../04-implementacion/pruebas/README.md#verificación-final-de-las-seis-operaciones-hu-026)).
- [x] Ambos motores probados con selección en ejecución: matriz de 96 combinaciones de HU-026 y pruebas de selección dinámica concurrente.
- [x] Scripts DDL/DML incluidos y reproducibles: `scripts/persona_ddl_*` y `scripts/persona_dml_*`, cargados en MariaDB y MongoDB (HU-004, PR #34).
- [x] README con preparación de entorno, bases, scripts, compilación y arranque (HU-029, PR #74, seguido desde un clon limpio).
- [x] Swagger 3 con los endpoints efectivos: `/api-docs` publica OpenAPI 3.1.0 con 17 rutas (HU-028, PR #66, #80 y #81).
- [ ] Informe académico: portada, marco conceptual, diseño, procedimiento, conclusiones, lecciones y referencias. Está completo en `main` (PR #79 y #82); falta registrar el commit y la URL del tag (HU-030).
- [x] Evidencias de compilación: JDK 11 de la plantilla ([HU-003 y HU-005](../04-implementacion/versiones.md)) y Java 25 con Spring Boot 4.1.2 de la entrega ([HU-007](../04-implementacion/versiones.md#migración-ejecutada-hu-007)).
- [ ] Diagnóstico de mapeos y pruebas en ambos motores: las pruebas están en `main`; falta confirmar contra el enunciado qué diagnóstico de mapeos exige.
- [ ] Tag final y URL de la versión entregada: se crea tras el ensayo de la demostración (HU-031).

**Estado actual:** 8 de 11 casillas verificadas con evidencia. Quedan el informe (a la espera del tag), el diagnóstico de mapeos y el tag (HU-032).
