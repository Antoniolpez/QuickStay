FROM maven:3.9-eclipse-temurin-19 AS builder

LABEL maintainer="Antonio López Montes"
LABEL description="QuickStay Java Application Server"

WORKDIR /build

# Copiar pom.xml y descargar dependencias
COPY jmapviewer-2.25.jar .
COPY pom.xml .

RUN mvn install:install-file -Dfile=jmapviewer-2.25.jar \
    -DgroupId=org.openstreetmap.jmapviewer \
    -DartifactId=jmapviewer \
    -Dversion=2.25 \
    -Dpackaging=jar

RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src

RUN rm -f src/main/java/module-info.java


RUN mvn package -DskipTests \
    && mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency

# ===========================================================================
# Imagen final de ejecución
# ===========================================================================
FROM eclipse-temurin:19-jre

WORKDIR /app

RUN apt-get update && apt-get install -y \
    zabbix-agent \
    openssl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Copiar JAR compilado y dependencias runtime
COPY --from=builder /build/target/*.jar /app/app.jar
COPY --from=builder /build/target/dependency/ /app/lib/

# Copiar configuración
COPY src/main/resources/config.properties.template /app/config.properties
COPY src/main/resources/log4j2.xml /app/log4j2.xml

# Script de inicio
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 1234

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
