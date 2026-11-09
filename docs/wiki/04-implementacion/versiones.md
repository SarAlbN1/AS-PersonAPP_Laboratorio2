# Versiones y estrategia de migración

**Entrega:** conservar JDK 11 y verificar la plantilla. HU-007 a HU-010 quedan diferidas.

| Componente | Línea base citada | Versión final validada | Evidencia |
|---|---|---|---|
| Java | 11 | Pendiente | `java -version` |
| Spring Boot | 2.7.11 | Pendiente | POM y compilación |
| Maven | Por registrar | Pendiente | `mvn -version` |
| Springdoc | 1.7.0 en REST | Pendiente | POM/Swagger |
| Driver MariaDB | 3.1.4 (POM Maria) | Pendiente | POM/prueba |
| Starter MongoDB | 2.7.11 (POM Mongo; no equivale a versión del servidor ni del driver transitivo) | Pendiente | POM/prueba |

El profesor menciona Java 21 y Spring Boot 4 como objetivos de modernización. La versión realmente utilizada debe fijarse después de verificar compatibilidad: ninguna de esas opciones queda aprobada por incluirla aquí.

## Declaración frente a entorno observado

POM padre: Spring Framework 5.3.27, Lombok 1.18.26, Log4j-to-SLF4J 2.20.0. CLI declara JAXB API 2.3.1 y core/impl 2.3.0.1. Java instalado observado: OpenJDK 25.0.4.1; Maven no encontrado en PATH. No hay Wrapper ni pruebas en src/test. Esto no demuestra compatibilidad de la plantilla con Java 25. No se migró ninguna dependencia.

El enunciado escrito exige JDK 11. Java 21 y Boot 4 quedan como opciones de trabajo posterior.
