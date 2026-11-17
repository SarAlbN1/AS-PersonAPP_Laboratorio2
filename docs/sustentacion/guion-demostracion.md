# Guion de la demostración · PersonAPP

Guion para mostrar PersonAPP en la sustentación: qué ejecutar, qué decir y qué debe verse en cada paso. Cubre la HU-031. Todos los comandos y resultados se **ensayaron el 17 de noviembre de 2026** sobre un clon limpio de `main` (`03fcc2a`) con el README vigente; el registro está al final.

**Duración prevista:** 15 minutos más preguntas. **Qué se demuestra:** los cuatro recursos con sus seis operaciones por REST y CLI, la selección del motor por petición y por menú, el manejo de errores, la actualización tecnológica, la contenerización y la arquitectura hexagonal.

## Antes de empezar (10 minutos antes)

Hacerlo una vez, con la red conectada, y **no** repetirlo delante del público.

- [ ] Docker Desktop abierto y sin otros contenedores en los puertos `3000`, `3307` y `27017`.
- [ ] Clon limpio de `main`, sin cambios locales: `git status` vacío.
- [ ] `.env` creado con una clave real: `cp .env.example .env` y cambiar `PERSONAPP_ROOT_PW`.
- [ ] Imágenes ya construidas: `docker compose build` y `docker compose --profile cli build`. Así el arranque de la demostración no depende de internet.
- [ ] Datos iniciales cargados: `docker compose down -v && docker compose up -d`, esperar a que `docker compose ps` muestre los tres servicios `healthy`, y luego `docker compose down` para dejar los volúmenes.
- [ ] JARs compilados por si hace falta el plan B: `./mvnw -B clean install` (deja `rest-input-adapter/target/*-exec.jar` y `cli-input-adapter/target/*-exec.jar`).
- [ ] Tres terminales abiertas en la carpeta del repositorio y el navegador con una pestaña vacía.
- [ ] Variable de rutas en la terminal de comandos: `B=http://localhost:3000/api/v1; J='Content-Type: application/json'`.
- [ ] Capturas o un video corto del recorrido, como respaldo si algo falla.

## Reparto sugerido

| Parte | Minutos | Quién |
|---|---|---|
| 0 Apertura y 1 Arranque | 3 | Miguel |
| 2 Swagger, 3 CRUD y 4 Selección dinámica | 7 | Sara |
| 5 Errores | 2 | Sara |
| 6 CLI | 2 | Juan |
| 7 Calidad y 8 Las cuatro fases | 3 | Juan y Miguel |
| 9 Cierre | 0,5 | Quien abrió |

Es una propuesta: ajustar según quién tenga qué historia más fresca.

---

## 0. Apertura (1 minuto)

**Qué decir.**
> PersonAPP es un servicio con arquitectura hexagonal para gestionar Persona, Profesión, Teléfono y Estudios. El profesor pidió cuatro cosas en orden: hacer funcionar la plantilla, actualizar sus tecnologías, contenerizarla y completar las funcionalidades. Conservamos los siete módulos Maven y sus adaptadores en todas las fases. Hoy mostramos el resultado funcionando en dos bases de datos distintas, MariaDB y MongoDB, que se eligen en cada petición.

**Qué mostrar.** Nada que ejecutar. Si hay pantalla, el índice de la wiki (`docs/wiki/Home.md`) o el diagrama de módulos de `docs/wiki/02-arquitectura`.

## 1. Arranque con contenedores (2 minutos)

**Qué ejecutar.**

```bash
docker compose up -d --build
docker compose ps
```

**Qué debe verse.** `mariadb`, `mongo` y `rest` en `healthy`. Solo REST publica puerto: `0.0.0.0:3000->3000`. Las bases no tienen puerto hacia afuera.

**Qué decir.**
> Con un solo comando se levantan las dos bases y la API. No hace falta instalar Java ni Maven: la imagen compila el proyecto con el Wrapper. Las bases cargan sus scripts DDL y DML la primera vez. Solo se publica el puerto de REST; las bases quedan dentro de la red de Compose.

**Dato para preguntas.** Java 25 y Spring Boot 4.1.2. Partimos de Java 11 y Spring Boot 2.7.11; la migración está documentada paso a paso en `docs/wiki/04-implementacion/versiones.md`.

## 2. Swagger (1,5 minutos)

**Qué hacer.** Abrir en el navegador `http://localhost:3000/swagger-ui.html`.

**Qué debe verse.** Los grupos Persona, Profesión, Teléfono y Estudios, cada uno con sus siete operaciones; en Persona, además, la consulta con sus teléfonos. `/api-docs` publica OpenAPI 3.1.0.

**Qué decir.**
> La documentación es la de la API real, generada del código. Cada operación declara sus códigos de respuesta con el catálogo de errores del equipo y el parámetro `database`, que es obligatorio y no tiene valor por defecto: la API nunca elige un motor por su cuenta.

**Mostrar una operación.** Abrir `POST /api/v1/persona`: el cuerpo de ejemplo, el `201` con `Location` y los errores `400`, `409` y `503`.

## 3. Un CRUD por entidad en REST (4 minutos)

Se usa una persona nueva que **no** existe en los datos iniciales, así que no depende del estado de la base.

**3.1 Persona: crear, consultar, editar (MariaDB).**

```bash
curl -s -X POST "$B/persona?database=MARIA" -H "$J" -d '{"dni":"100100100","firstName":"Demo","lastName":"Javeriana","age":"25","sex":"F"}'
curl -s "$B/persona/100100100?database=MARIA"
curl -s -X PUT "$B/persona/100100100?database=MARIA" -H "$J" -d '{"dni":"100100100","firstName":"Demo","lastName":"Javeriana","age":"26","sex":"F"}'
```

Resultado: `201`, `200` y `200` con la edad en `26`.

**3.2 Profesión, Teléfono y Estudios, ligados a esa persona.**

```bash
curl -s -X POST "$B/profesion?database=MARIA" -H "$J" -d '{"id":50,"name":"Ingenieria de Sistemas","description":"Demo"}'
curl -s -X POST "$B/telefono?database=MARIA"  -H "$J" -d '{"number":"0013000001","company":"Claro","ownerId":100100100}'
curl -s -X POST "$B/estudio?database=MARIA"   -H "$J" -d '{"professionId":50,"personId":100100100,"graduationDate":"2024-06-15","universityName":"Pontificia Universidad Javeriana"}'
curl -s -X PUT  "$B/estudio/50/100100100?database=MARIA" -H "$J" -d '{"professionId":50,"personId":100100100,"graduationDate":"2024-12-01","universityName":"Javeriana Bogota"}'
```

Resultado: tres `201` y un `200` en la edición del estudio.

**Qué decir.**
> Las cuatro entidades tienen las mismas seis operaciones: consultar por identificador, listar, contar, crear, editar y eliminar; el listado paginado es adicional. Dos detalles del modelo: el teléfono es texto, así que conserva los ceros iniciales (`0013000001`), y Estudios tiene clave compuesta, profesión y persona, que van en la ruta en ese orden. Al editar un estudio solo cambian la fecha y la universidad; la identidad no se mueve.

**3.3 Persona con todos sus teléfonos.**

```bash
curl -s "$B/persona/100100100/telefonos?database=MARIA"
```

Resultado: `200` con la persona y la lista `phones`. Es la consulta adicional de la HU-033.

## 4. Selección dinámica del motor (1,5 minutos)

**Qué ejecutar.**

```bash
curl -s "$B/persona/100100100?database=MONGO"
curl -s -X POST "$B/persona?database=MONGO" -H "$J" -d '{"dni":"100100100","firstName":"Demo Mongo","lastName":"Javeriana","age":"25","sex":"F"}'
curl -s "$B/persona/100100100?database=MONGO"
```

**Qué debe verse.** Primero `404 ERR-03`: *«No existe una persona con cc 100100100 en MONGO»*, aunque la persona existe en MariaDB. Después `201`, y la tercera llamada devuelve `Demo Mongo`.

**Qué decir.**
> La misma cédula existe en MariaDB con el nombre Demo y en MongoDB con otro, y cada petición lee del motor que pide. Los dos motores no se replican entre sí. El motor se elige por petición, sin reiniciar nada y sin estado compartido: lo comprobamos con pruebas de 3200 solicitudes de 16 hilos alternando motores, y ninguna respuesta cruza datos de uno al otro. Para confirmar que esa prueba sirve, reintrodujimos el campo compartido que usaba la plantilla para guardar el motor, y la prueba falló con más de mil respuestas cruzadas de 3200.

## 5. Manejo de errores (2 minutos)

Todos los errores usan el mismo cuerpo, `{"error":"ERR-xx","message":"..."}`, con el código HTTP del catálogo y sin trazas.

```bash
curl -s -X POST "$B/persona?database=MARIA" -H "$J" -d '{"dni":"100100100","firstName":"X","lastName":"Y","age":"1","sex":"M"}'       # ERR-04  409
curl -s -X POST "$B/estudio?database=MARIA"  -H "$J" -d '{"professionId":9999,"personId":100100100}'                                   # ERR-05  422
curl -s -X DELETE "$B/persona/100100100?database=MARIA"                                                                                  # ERR-06  409
curl -s -X POST "$B/estudio?database=MARIA"  -H "$J" -d '{"professionId":50,"personId":123456789,"graduationDate":"15/06/2024"}'        # ERR-01  400
curl -s "$B/persona?database=ORACLE"                                                                                                     # ERR-02  400
curl -s "$B/persona/999999999?database=MARIA"                                                                                            # ERR-03  404
```

| Código | Qué muestra | Mensaje observado |
|---|---|---|
| `409 ERR-04` | Clave duplicada: no se sobrescribe | *Ya existe una persona con cc 100100100 en MARIA* |
| `422 ERR-05` | Referencia inexistente en el mismo motor | *No existe una profesión con id 9999 en MARIA* |
| `409 ERR-06` | Borrado restrictivo: la persona tiene teléfono y estudio | *La persona con cc 100100100 tiene teléfonos o estudios asociados en MARIA* |
| `400 ERR-01` | Dato inválido, con el formato esperado | *La fecha '15/06/2024' no es válida; use el formato yyyy-MM-dd* |
| `400 ERR-02` | Motor desconocido: no hay motor por defecto | *El motor ORACLE no es válido; use MARIA o MONGO* |
| `404 ERR-03` | Registro inexistente | *No existe una persona con cc 999999999 en MARIA* |

**Qué decir.**
> El borrado es restrictivo: no se puede eliminar una persona que tiene teléfonos o estudios. En MariaDB lo respaldaría la clave foránea, pero MongoDB no tiene ese mecanismo, así que la regla vive en el caso de uso y vale igual para los dos motores. Antes de corregirlo, el borrado se llevaba los dependientes en MariaDB y los dejaba huérfanos en MongoDB; nos lo encontró la revisión de un compañero con las bases reales.

**Limpieza al terminar la sección** (orden: primero los dependientes):

```bash
curl -s -X DELETE "$B/estudio/50/100100100?database=MARIA"
curl -s -X DELETE "$B/telefono/0013000001?database=MARIA"
curl -s -X DELETE "$B/profesion/50?database=MARIA"
curl -s -X DELETE "$B/persona/100100100?database=MARIA"
curl -s -X DELETE "$B/persona/100100100?database=MONGO"
```

Resultado: cinco `204`. La limpieza también sirve de demostración: ahora sí se puede borrar la persona.

## 6. La CLI (2 minutos)

**Qué ejecutar** (en una terminal aparte, con la API ya arriba).

```bash
docker compose run --rm --build cli
```

Teclas, en este orden. Cada línea es una pantalla distinta:

| Tecla | Qué hace |
|---|---|
| `1` | Menú Persona |
| `1` | Motor MariaDB |
| `1` y `123456789` | Consultar por identificador |
| `3` | Contar |
| `0` | Volver al menú de motor |
| `2` | Motor MongoDB, **sin reiniciar la aplicación** |
| `3` | Contar |
| `1` y `147258369` | Consultar |
| `1` y `999` | Error controlado |
| `0`, `0`, `0` | Salir |

**Qué debe verse.**

```
---------------------- Persona · MARIA
Persona [cc=123456789, nombre=Pepe, apellido=Perez, género=MALE, edad=30]
Total en MARIA: 5
---------------------- Persona · MONGO
Total en MONGO: 5
Persona [cc=147258369, nombre=Pepita, apellido=Juarez, género=FEMALE, edad=10]
[ERR-03] No existe una persona con cc 999 en MONGO
```

**Qué decir.**
> La CLI ofrece las mismas operaciones y el mismo menú para los cuatro recursos. Se elige el motor y se puede cambiar en la misma sesión. Los errores salen con su código, igual que en REST, y la sesión sigue viva.

**Importante.** El `--build` evita que Docker abra una imagen de la CLI de una versión anterior.

## 7. Calidad: pruebas y arquitectura (1,5 minutos)

**Qué ejecutar** (o mostrar el resultado ya corrido).

```bash
./mvnw -B clean install                                              # sin bases
docker compose -f compose.yaml -f compose.pruebas.yaml up -d mariadb mongo
./mvnw -B clean install -Dmotores=arriba                             # contra bases reales
docker compose -f compose.yaml -f compose.pruebas.yaml down -v
```

**Qué debe verse.** `BUILD SUCCESS` las dos veces. Sin bases corren **177** pruebas; con `-Dmotores=arriba` corren **223**, porque se suman 46 pruebas que usan MariaDB y MongoDB reales y no motores simulados.

**Qué decir.**
> Las pruebas de los adaptadores se ejecutan contra las bases de verdad, no contra simuladores. Las que necesitan bases solo corren cuando se piden, así que compilar sin ellas no falla. Además de las funcionales, hay pruebas de concurrencia para la selección del motor y de recursión de los mappers, un defecto que traía la plantilla y que reventaba la API en cuanto una persona tenía estudios.

**La arquitectura se puede auditar.** La sección *Revisión de HU-027* de `docs/wiki/02-arquitectura/reglas-arquitectonicas.md` trae cinco comandos que deben dar cero líneas: el dominio sin dependencias de Spring ni de motores, `application` sin Spring, JPA ni Mongo, y los adaptadores sin importarse entre sí.

## 8. Las cuatro fases del profesor (1,5 minutos)

| Fase | Qué se hizo | Dónde se ve |
|---|---|---|
| 1. Hacer funcionar la plantilla | Se corrigió el defecto del mapper REST de Persona, que devolvía un objeto vacío, y se agregó su prueba de regresión | `docs/wiki/04-implementacion/ejecucion-local.md`, HU-006 |
| 2. Actualizar las tecnologías | Java 11 a 25, Spring Boot 2.7.11 a 4.1.2, Hibernate 5 a 7, `javax` a `jakarta`, Springdoc 1 a 3, JUnit 5 a 6. Cuatro incompatibilidades encontradas y resueltas | `docs/wiki/04-implementacion/versiones.md` |
| 3. Contenerizar | `Dockerfile` multietapa y `compose.yaml` con las dos bases, volúmenes y configuración por variables | Paso 1 de esta demostración |
| 4. Completar las funcionalidades | Los cuatro recursos con seis operaciones más el listado parcial, por REST y CLI, en los dos motores | Pasos 3 a 6 |

**Qué decir.**
> En las cuatro fases conservamos los siete módulos Maven, sus responsabilidades y los adaptadores. La actualización no fue cambiar números de versión: hubo que migrar `javax` a `jakarta`, cambiar el artefacto de Springdoc, dejar de leer la conexión de MongoDB por las propiedades antiguas y declarar el procesador de Lombok, porque desde Java 23 el procesamiento implícito de anotaciones viene apagado.

## 9. Cierre (30 segundos)

**Qué decir.**
> Mostramos los cuatro recursos con sus seis operaciones por REST y por CLI, dos motores de persistencia elegidos en cada petición, el manejo de errores con códigos propios, y todo levantado con un solo comando. El repositorio, el informe y la wiki con las decisiones, los contratos y las evidencias están a disposición.

---

## Preguntas probables

| Pregunta | Respuesta corta |
|---|---|
| ¿Por qué dos motores? | El laboratorio pide elegir entre MariaDB (relacional) y MongoDB (documental) en ejecución, sin reiniciar. La arquitectura hexagonal lo permite porque el núcleo solo conoce puertos |
| ¿Cómo se elige el motor? | Con el parámetro `database` en REST y con el menú en la CLI. No hay motor por defecto: sin él, `400 ERR-02` |
| ¿Se replican los datos entre motores? | No. Cada motor tiene los suyos, y el paso 4 lo muestra |
| ¿Qué pasa si una base cae? | `503 ERR-07` y no se salta al otro motor |
| ¿Por qué `OTHER` no se admite como género? | El `ENUM` de MariaDB solo acepta `M` y `F`; es la decisión D-01 y se rechaza con `400 ERR-01` |
| ¿Cómo garantizan que la arquitectura se conserva? | Los cinco comandos de auditoría de HU-027 y que los siete módulos siguen igual que en la plantilla |
| ¿Qué cambió respecto a la plantilla? | Las cuatro fases del paso 8; el detalle está en la wiki y en el informe |
| ¿Dónde está la evidencia? | `docs/wiki/04-implementacion/ejecucion-local.md` y `registro-de-evidencias.md`, y los PR de cada historia |

## Plan de recuperación

Cada fila se ensayó el 17 de noviembre de 2026 salvo las marcadas como *no ensayado*.

| Si pasa… | Hacer | Tiempo |
|---|---|---|
| Los datos quedaron sucios por un ensayo o un error | `docker compose down -v && docker compose up -d`. Restaura los datos iniciales | **12 s** ensayado |
| El puerto `3000` está ocupado | Poner `PERSONAPP_REST_PORT=3001` en `.env` y repetir `docker compose up -d`; usar `3001` en las URLs | **ensayado**: responde `200` en `3001` |
| Un servicio no queda `healthy` | `docker compose logs <servicio>`, luego `docker compose down -v && docker compose up -d` | no ensayado |
| Docker no arranca o no hay red | Plan B sin contenedores: bases locales y `java -jar rest-input-adapter/target/rest-input-adapter-*-exec.jar` (README, sección *Sin contenedores para la aplicación*). Por eso los JAR deben estar compilados desde antes | no ensayado |
| La CLI no acepta la entrada | `docker compose run --rm --build cli`; si el terminal no es interactivo, usar `-T` y pasar las teclas por entrada estándar | **ensayado** con `-T` |
| Un `curl` falla en directo | Repetirlo desde Swagger UI con *Try it out*, que usa los mismos datos | no ensayado |
| Falla todo | Mostrar las capturas o el video de respaldo y el informe con las evidencias | no ensayado |

**Regla de oro.** Si algo falla, no depurar en público: nombrar el problema en una frase, aplicar la fila de la tabla y continuar. Los datos de la demostración son desechables.

## Registro del ensayo

| Dato | Valor |
|---|---|
| Fecha | 17 de noviembre de 2026 |
| Entorno | macOS ARM64, Docker Desktop, Java 25.0.4 y Maven 3.10.0 por el Wrapper |
| Código | Clon limpio de `main` en `03fcc2a`, con el README de la HU-029; imágenes y volúmenes borrados antes |
| Arranque | `docker compose up -d --build`: los tres servicios `healthy`, solo REST publicado |
| Swagger | `swagger-ui.html` y `/api-docs` con `200`; OpenAPI 3.1.0 con 17 rutas de los cuatro recursos |
| Pasos 3 a 5 | Los 27 pasos de REST dieron el código esperado, en este orden y sin intervención |
| Paso 6 | Sesión de CLI con el cambio de MariaDB a MongoDB sin reiniciar y el `ERR-03` esperado |
| Paso 7 | `BUILD SUCCESS` sin bases con 177 pruebas y con `-Dmotores=arriba` con 223, sin fallos |
| Recuperación | `down -v && up -d` restauró los datos en 12 s; con el puerto cambiado a `3001` responde `200` |

**Pendiente.** Un ensayo cronometrado con los tres presentando, para ajustar el reparto de tiempos de la tabla inicial.
