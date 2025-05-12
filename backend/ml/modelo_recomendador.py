import pandas as pd
import joblib
from sklearn.ensemble import RandomForestClassifier
import mysql.connector

# 1. Conexión a la base de datos MySQL
conexion = mysql.connector.connect(
    host="localhost",
    port=3306,
    user="root",
    password="admin123",
    database="gestion_reservas"
)

# 2. Consulta el historial de reservas
query = """
SELECT
    r.id_usuario AS usuario_id,
    HOUR(r.fecha_inicio) AS hora,
    DAYOFWEEK(r.fecha_inicio) - 1 AS dia_semana,  -- domingo=1 a sábado=7 → normalizamos a 0-6
    r.id_espacio AS espacio_id
FROM reservas r
"""

df = pd.read_sql(query, conexion)
conexion.close()

# 3. Aseguramos que 'hora' siempre tiene un valor (aunque sea -1)
df["hora"] = df["hora"].fillna(-1)

# 4. Separamos features (X) y etiqueta (y)
X = df[["usuario_id", "hora", "dia_semana"]]
y = df["espacio_id"]

# 5. Entrenamos el modelo
modelo = RandomForestClassifier(n_estimators=100, random_state=42)
modelo.fit(X, y)

# 6. Guardamos el modelo en disco
joblib.dump(modelo, "modelo.pkl")

print("Modelo entrenado y guardado como 'modelo.pkl'")
