import sys
import pandas as pd
import joblib
import mysql.connector

# 1. Cargamos el modelo desde disco
modelo = joblib.load("modelo.pkl")

# 2. Leemos los argumentos
# Uso: python predictor.py <usuario_id> <dia_semana> [hora]
usuario_id = int(sys.argv[1])
dia_semana = int(sys.argv[2])
hora = int(sys.argv[3]) if len(sys.argv) > 3 else -1  # si no se pasa, -1

# 3. Creamos entrada para el modelo
entrada = pd.DataFrame([[usuario_id, hora, dia_semana]],
                       columns=["usuario_id", "hora", "dia_semana"])

# 4. Predecimos el espacio recomendado
prediccion = modelo.predict(entrada)
espacio_id = int(prediccion[0])

# 5. Conectamos a la base de datos para obtener nombres y guardar recomendación
conexion = mysql.connector.connect(
    host="localhost",
    port=3306,
    user="root",
    password="admin123",
    database="gestion_reservas"
)
cursor = conexion.cursor()

# Obtenemos el nombre del usuario
cursor.execute("SELECT nombre FROM usuarios WHERE id_usuario = %s", (usuario_id,))
resultado_usuario = cursor.fetchone()
nombre_usuario = resultado_usuario[0] if resultado_usuario else "Desconocido"

# Obtenemos el nombre del espacio
cursor.execute("SELECT nombre FROM espacios WHERE id_espacio = %s", (espacio_id,))
resultado_espacio = cursor.fetchone()
nombre_espacio = resultado_espacio[0] if resultado_espacio else "Desconocido"

# Guardamos la recomendación en la tabla
cursor.execute("""
    INSERT INTO recomendaciones
    (id_usuario, nombre_usuario, dia_semana, hora, id_espacio, nombre_espacio)
    VALUES (%s, %s, %s, %s, %s, %s)
""", (usuario_id, nombre_usuario, dia_semana, hora, espacio_id, nombre_espacio))

conexion.commit()

# Cerramos la conexión
cursor.close()
conexion.close()

# 6. Mostramos los resultados
print(f"Usuario: {nombre_usuario} (ID: {usuario_id})")
print(f"Día de la semana: {dia_semana} | Hora: {hora}")
print(f"Espacio recomendado: {nombre_espacio} (ID: {espacio_id})")
