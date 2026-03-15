FROM python:3.9-slim

WORKDIR /app

# Instalar herramientas de red necesarias
RUN apt-get update && apt-get install -y --no-install-recommends \
    iproute2 \
    nftables \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Copiar backend
COPY backend/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY backend/app.py .
COPY frontend/index.html ./static/

EXPOSE 5000

CMD ["python", "app.py"]
