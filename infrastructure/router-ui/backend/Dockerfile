FROM python:3.9-slim

WORKDIR /app

# Instalar herramientas de red necesarias
RUN apt-get update && apt-get install -y --no-install-recommends \
    iproute2 \
    nftables \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Copiar requirements y instalar dependencias Python
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar aplicación
COPY app.py .

# Crear usuario no-root
RUN useradd -m -u 1000 routeradmin
USER routeradmin

EXPOSE 5000

CMD ["python", "app.py"]
