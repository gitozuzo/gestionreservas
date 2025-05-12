import sys
import pandas as pd
import joblib
import mysql.connector
import json

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

# 4. Obtenemos las probabilidades
probabilidades = modelo.predict_proba(entrada)[0]

# 5. Tomamos el top 3 de espacios recomendados
top_n = 3
top_indices = sorted(range(len(probabilidades)), key=lambda i: -probabilidades[i])[:top_n]
top_salas = [(modelo.classes_[i], probabilidades[i]) for i in top_indices]

# 6. Conectamos a la base de datos
conexion = mysql.connector.connect(
    host="localhost",
    port=3306,
    user="root",
    password="admin123",
    database="gestion_reservas"
)
cursor = conexion.cursor()

# 7. Obtenemos el nombre del usuario
cursor.execute("SELECT nombre FROM usuarios WHERE id_usuario = %s", (usuario_id,))
resultado_usuario = cursor.fetchone()
nombre_usuario = resultado_usuario[0] if resultado_usuario else "Desconocido"

# 8. Procesamos recomendaciones y las guardamos en la tabla
recomendaciones = []
for espacio_id, prob in top_salas:
    cursor.execute("SELECT nombre FROM espacios WHERE id_espacio = %s", (int(espacio_id),))
    resultado_espacio = cursor.fetchone()
    nombre_espacio = resultado_espacio[0] if resultado_espacio else "Desconocido"

    # Agregamos al listado de salida
    recomendaciones.append({
        "espacio_id": int(espacio_id),
        "nombre_espacio": nombre_espacio,
        "probabilidad": round(prob, 3)
    })

    # Insertamos en la tabla recomendaciones
    cursor.execute("""
        INSERT INTO recomendaciones
        (id_usuario, nombre_usuario, dia_semana, hora, id_espacio, nombre_espacio)
        VALUES (%s, %s, %s, %s, %s, %s)
    """, (
        int(usuario_id),
        str(nombre_usuario),
        int(dia_semana),
        int(hora),
        int(espacio_id),
        str(nombre_espacio)
    ))

# 9. Guardamos y cerramos conexión
conexion.commit()
cursor.close()
conexion.close()

# 10. Imprimimos el resultado en JSON (respuesta al backend)
salida = {
    "usuario_id": usuario_id,
    "nombre_usuario": nombre_usuario,
    "dia_semana": dia_semana,
    "hora": hora,
    "recomendaciones": recomendaciones
}

print(json.dumps(salida))
