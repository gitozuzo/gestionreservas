# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

# Instalar Python, venv y pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-venv && \
    python3 -m venv /opt/venv && \
    /opt/venv/bin/pip install --upgrade pip && \
    /opt/venv/bin/pip install --no-cache-dir -r ml/requirements_postgresql.txt

RUN chmod +x mvnw && \
    ./mvnw clean install -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Instalar Python, venv y pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-venv

# Crear entorno virtual en imagen final
RUN python3 -m venv /opt/venv

# Copiar app y entorno virtual
COPY --from=build /app/target/reservas-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/ml ml
COPY --from=build /opt/venv /opt/venv

# Asegurar que se use el pip del entorno virtual
ENV PATH="/opt/venv/bin:$PATH"

# Exponer puerto
EXPOSE 8080

# Ejecutar Spring Boot
CMD ["java", "-jar", "app.jar"]
