# Ejecución local: plan de reproducción

> **Resultado de inspección:** no se ejecutaron build, aplicaciones ni scripts. Maven no está en PATH; Java observado es 25.0.4.1. La entrega se verifica en JDK 11. El DML Mongo usa prueba_db, las aplicaciones persona_db; el DDL Maria elimina esquema y usuario.

## Fase 1: línea base sin migraciones

1. Crear fork del repositorio del profesor: `https://github.com/andres-karoll/personapp-hexa-spring-boot`.
2. Instalar JDK 11, Maven y los servidores MariaDB y MongoDB requeridos.
3. Registrar `java -version`, `mvn -version` y versión de ambos motores.
4. Inspeccionar y ejecutar DDL/DML, verificando el nombre de la base MongoDB.
5. Ejecutar `mvn clean install` desde la raíz multimódulo.
6. Arrancar y probar por separado `PersonAppRestApi` y `PersonAppCli`.
7. Revisar `http://localhost:3000/swagger-ui.html` en la configuración de referencia.
8. Reproducir, diagnosticar y corregir el error de Mapper indicado por el profesor.

## Trabajo diferido después de la entrega: migración

Actualizar Java, Spring Boot, drivers, JPA/paquetes y librerías solo después de preservar la línea base. Registrar cambios de POM, errores y resolución.

## Fase 3: funcionalidad faltante

Completar CRUD y exposición REST/CLI para Persona, Profesión, Estudios y Teléfono en MariaDB y MongoDB.

**Pendiente:** no se dispone de registros de ejecución del equipo; añadir comandos realmente usados, fallos, salidas y capturas.
