# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Crear directorio de trabajo
WORKDIR /app

# Copiar el código fuente
COPY . .

# Instalar Python y actualizar pip en la etapa de build
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    python3 -m pip install --upgrade pip && \
    pip3 install --break-system-packages -r ml/requirements_postgresql.txt

# Dar permisos al wrapper de Maven
RUN chmod +x mvnw

# Construir el proyecto Java (sin tests)
RUN ./mvnw clean install -DskipTests

# Etapa de ejecución (final)
FROM eclipse-temurin:17-jdk

# Instalar Python y actualizar pip en la imagen final
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    ln -s /usr/bin/python3 /usr/bin/python && \
    python3 -m pip install --upgrade pip

# Crear directorio de trabajo
WORKDIR /app

# Copiar artefactos Java y scripts Python
COPY --from=build /app/target/reservas-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/ml ml

# 🔧 Instalar dependencias Python necesarias en producción
RUN pip3 install --break-system-packages -r ml/requirements_postgresql.txt

# Exponer el puerto de la app
EXPOSE 8080

# Comando para ejecutar Spring Boot
CMD ["java", "-jar", "app.jar"]

