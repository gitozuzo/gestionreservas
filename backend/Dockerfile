# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Crear directorio de trabajo
WORKDIR /app

# Copiar el código fuente
COPY . .

# Instalar Python, wget y pip manualmente desde get-pip.py
RUN apt-get update && \
    apt-get install -y python3 python3-pip wget && \
    wget https://bootstrap.pypa.io/get-pip.py && \
    python3 get-pip.py && \
    rm get-pip.py

# Instalar dependencias de Python
RUN pip install -r ml/requirements_postgresql.txt

# Dar permisos al wrapper de Maven
RUN chmod +x mvnw

# Construir el proyecto Java (sin tests)
RUN ./mvnw clean install -DskipTests

# Etapa de ejecución (final)
FROM eclipse-temurin:17-jdk

# Instalar Python, wget y pip manualmente en la imagen final
RUN apt-get update && \
    apt-get install -y python3 python3-pip wget && \
    wget https://bootstrap.pypa.io/get-pip.py && \
    python3 get-pip.py && \
    rm get-pip.py

# Crear directorio de trabajo
WORKDIR /app

# Copiar artefactos Java y scripts Python
COPY --from=build /app/target/reservas-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/ml ml

# Instalar dependencias de Python en la imagen final
RUN pip install -r ml/requirements_postgresql.txt

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar Spring Boot
CMD ["java", "-jar", "app.jar"]
