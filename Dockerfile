# Imágenes de PersonAPP (HU-034).
# Una sola construcción del reactor de siete módulos y dos imágenes de ejecución:
#   docker build --target rest -t personapp-rest .
#   docker build --target cli  -t personapp-cli  .
# Los módulos Maven no se convierten en servicios: cada imagen empaqueta una entrada.

FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY . .
# El Wrapper fija Maven 3.10.0 (HU-007); la caché evita descargar dependencias en cada construcción.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp clean package

FROM eclipse-temurin:25-jre AS runtime
RUN groupadd --system personapp \
    && useradd --system --gid personapp --home-dir /app personapp \
    && mkdir -p /app/logs \
    && chown -R personapp:personapp /app
WORKDIR /app
USER personapp

FROM runtime AS rest
COPY --from=build /src/rest-input-adapter/target/rest-input-adapter-*-exec.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM runtime AS cli
COPY --from=build /src/cli-input-adapter/target/cli-input-adapter-*-exec.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
