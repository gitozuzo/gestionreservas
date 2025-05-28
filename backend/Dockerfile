# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Crear directorio de trabajo
WORKDIR /app

# Copiar el código fuente
COPY . .

# Instalar Python y dependencias
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install -r ml/requirements_postgresql.txt

# Asignar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Construir el proyecto Java
RUN ./mvnw clean install -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jdk

# Instalar Python en la imagen final
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    ln -s /usr/bin/python3 /usr/bin/python

# Crear directorio de trabajo
WORKDIR /app

# Copiar artefactos desde la fase de build
COPY --from=build /app/target/reservas-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/ml ml

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar Spring Boot
CMD ["java", "-jar", "app.jar"]

