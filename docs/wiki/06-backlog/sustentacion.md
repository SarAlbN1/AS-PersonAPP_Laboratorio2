# Preparación de demostración y defensa

[← Backlog](README.md) · [Guía de pruebas](../04-implementacion/runbook-demo.md)

## Recorrido propuesto (no ejecutado todavía)

1. Identificar tag, commit y versiones efectivas; enseñar compilación Maven y arranque REST/CLI.
2. Mostrar Swagger con endpoints reales de persona, profesión, estudios y teléfono.
3. Crear/consultar/editar/eliminar registros de los cuatro recursos y conservar las relaciones.
4. Repetir operaciones seleccionando MariaDB y MongoDB en solicitudes alternadas sin reiniciar.
5. En la misma sesión CLI cambiar entre ambas bases y verificar resultados.
6. Mostrar errores controlados: duplicado, referencia inexistente, clave compuesta y datos inválidos.
7. Explicar límites: arquitectura hexagonal, puertos de entrada/salida, adaptadores, Repository/Service; explicar el uso de JDK 11.
8. Mostrar las evidencias de las seis operaciones y el README desde un clon limpio, luego URL del tag.

## Preguntas de verificación

- ¿Dónde está implementada la selección de base y por qué no obliga a recompilar?
- ¿Qué prueba demuestra que un Mapper es correcto y que se corrigió el error original?
- ¿Qué dependencias impiden que el dominio conozca JPA o MongoDB?
- ¿Cómo se garantiza que la clave compuesta de Estudios conserva identidad en ambos motores?
- ¿Con qué versiones se ejecutó la entrega y qué se verificó?
- ¿Por qué dos clientes REST/CLI no implican microservicios?

## Plan de contingencia

Registrar antes de la demo un conjunto mínimo de datos, comandos locales y capturas **reales**. No sustituir demostración inexistente por imágenes fabricadas. Si un motor falla, indicar el error y mostrar evidencia de última ejecución verificada, dejando claro que no se está demostrando en vivo.

**Propiedad del guion:** HU-031, auxiliar P1; el requisito de informe y entrega sigue siendo obligatorio.
