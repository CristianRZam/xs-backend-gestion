# Usa un JDK base (Java 17)
FROM eclipse-temurin:17-jdk-jammy

# Carpeta de trabajo dentro del contenedor
WORKDIR /app

# Copia tu WAR generado al contenedor
COPY target/sistema-0.0.1-SNAPSHOT.war app.war

# Expone el puerto que tu app usará
EXPOSE 8080

# Comando para correr tu WAR
CMD ["java", "-jar", "app.war"]
