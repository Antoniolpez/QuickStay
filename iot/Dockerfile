FROM python:3.11-slim

LABEL maintainer="Antonio López Montes"
LABEL description="QuickStay IoT Simulator"

WORKDIR /app

# Instalar dependencias
RUN pip install --no-cache-dir paho-mqtt

# Copiar script del simulador
COPY iot_client.py .

CMD ["python", "-u", "iot_client.py"]
