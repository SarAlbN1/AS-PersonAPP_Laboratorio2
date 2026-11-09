# ADR-001 · Estrategia de migración tecnológica

**Estado:** Propuesto; **versiones finales aún no aprobadas ni verificadas**  
**Historias:** HU-005, HU-006, HU-007, HU-008, HU-009, HU-010.

## Contexto

El proyecto original reporta Java 11 y Spring Boot 2.7.11. El profesor pide actualización a versiones modernas y menciona Java 21 y Boot 4. El salto puede afectar APIs Jakarta, Spring Data, documentación OpenAPI, Lombok, drivers y Maven. El baseline contiene además un defecto de mapper que debe aislarse.

## Opciones

1. Migrar todo antes de probar: rápido en apariencia, impide aislar regresiones.
2. **Propuesta:** baseline → diagnóstico Mapper → matriz de compatibilidad → migración escalonada → pruebas REST/CLI/Maria/Mongo/Swagger.
3. Conservar versiones antiguas: no satisface el objetivo de modernización indicado.

## Decisión propuesta

Adoptar la opción 2, condicionada a aprobación del equipo. No fijar versión exacta hasta verificar POM, compatibilidad documental y compilación real.

## Criterios de aprobación/verificación

- HU-005 registra ejecución original; HU-006 prueba el Mapper.
- HU-007 fija versiones exactas elegidas y razones.
- HU-008/009 adaptan POM y conectores; HU-010 valida arranques y Swagger.
- Evidencia: `java -version`, `mvn -version`, compilación, ejecución, pruebas y URL PR/commit.

**Consecuencia:** más pasos iniciales a cambio de diagnóstico reproducible y menor riesgo de mezclar defectos.
