# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Crear directorio de trabajo
WORKDIR /app

# Copiar el código fuente
COPY . .

# Instalar Python y pip desde repositorios oficiales
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install --no-cache-dir -r ml/requirements_postgresql.txt

# Dar permisos al wrapper de Maven
RUN chmod +x mvnw

# Construir el proyecto Java (sin tests)
RUN ./mvnw clean install -DskipTests

# Etapa de ejecución (imagen final)
FROM eclipse-temurin:17-jdk

# Instalar Python y pip desde repos oficiales (sin get-pip.py)
RUN apt-get update && \
    apt-get install -y python3 python3-pip

# Crear directorio de trabajo
WORKDIR /app

# Copiar artefactos y scripts
COPY --from=build /app/target/reservas-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/ml ml

# Instalar dependencias de Python en producción
RUN pip3 install --no-cache-dir -r ml/requirements_postgresql.txt

# Exponer puerto de la app
EXPOSE 8080

# Comando de ejecución
CMD ["java", "-jar", "app.jar"]
