# ADR-002 · Selección dinámica de MariaDB/MongoDB

**Estado:** Propuesto · HU-025 · Requisito RF-05

## Contexto

El docente exige poder seleccionar MariaDB o MongoDB **durante la ejecución**, sin recompilación ni reinicio. Los puertos y adaptadores ya forman parte del diseño; falta corroborar cómo el código resuelve cada implementación.

## Alternativas

| Alternativa | Ventaja | Riesgo |
|---|---|---|
| Perfil Spring elegido al arrancar | Configuración simple | No satisface cambio en caliente |
| Mutar variable global que cambia el bean activo | Menos parámetros explícitos | Carreras y peticiones afectadas entre sí |
| **Registro/estrategia inmutable por operación** | Determinista por solicitud; testable | Añade resolución explícita |

## Decisión propuesta

Resolver `MARIA`/`MONGO` por operación o acción de menú, a través de abstracciones de aplicación, sin conservar estado global mutable que altere peticiones de otro usuario. No asumir detalles de implementación antes de estudiar el fork.

## Verificación

Escenario A-B-A en REST y CLI en una misma ejecución, peticiones intercaladas donde corresponda, motor inválido y motor indisponible. Registrar resultados reales y PR. No implica CQRS ni replicación de datos.
