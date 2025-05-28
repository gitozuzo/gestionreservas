
# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

# Instalar Python, venv y pip en etapa de build
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-venv && \
    python3 -m venv /opt/venv && \
    /opt/venv/bin/pip install --upgrade pip && \
    /opt/venv/bin/pip install --no-cache-dir -r ml/requirements_postgresql.txt

# Construir aplicación Java
RUN chmod +x mvnw && ./mvnw clean install -DskipTests

# Etapa de ejecución (final)
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Instalar Python, venv y pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-venv

# Copiar entorno virtual completo desde la etapa de build
COPY --from=build /opt/venv /opt/venv

# Copiar la aplicación y scripts Python
COPY --from=build /app/target/reservas-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/ml ml

# Activar el entorno virtual como PATH por defecto
ENV PATH="/opt/venv/bin:$PATH"

# 👉 Verificación de que pandas está instalado
RUN /opt/venv/bin/python -c "import pandas; print('Pandas instalado, versión:', pandas.__version__)"

# Exponer puerto
EXPOSE 8080

# Comando para iniciar Spring Boot
CMD ["java", "-jar", "app.jar"]
