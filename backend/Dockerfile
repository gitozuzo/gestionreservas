# Etapa 1: Build de Java + instalación de Python + pip
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .

# Instalar Python y dependencias
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install -r ml/requirements.txt

# Construir la app Java
RUN ./mvnw clean install -DskipTests

# Etapa 2: Imagen de producción
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiar app compilada
COPY --from=build /app/target/reservas-0.0.1-SNAPSHOT.jar app.jar

# Copiar scripts Python y directorio de modelo generado
COPY --from=build /app/ml ml

# Copiar imágenes u otros recursos necesarios
COPY --from=build /app/uploads uploads

# Instalar Python en producción
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install -r ml/requirements.txt

# Exponer puerto
EXPOSE 8080

# Ejecutar la app Java
CMD ["java", "-jar", "app.jar"]
