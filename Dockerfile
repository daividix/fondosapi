# Usar JRE ligero
FROM eclipse-temurin:17-jre-jammy

# Argumento: el jar generado por Maven
ARG JAR_FILE=target/fondosapi-0.0.1-SNAPSHOT.jar

# Copiar el jar al contenedor
COPY ${JAR_FILE} /app/app.jar

# Puerto expuesto
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java","-jar","/app/app.jar"]
