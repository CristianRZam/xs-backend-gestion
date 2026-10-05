# Java 17
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo del backend
WORKDIR /app

# WAR de D'Primera
COPY target/dprimera-api.war app.war

# Puerto interno de Spring Boot
EXPOSE 8080

# Ejecutar aplicación
CMD ["java", "-jar", "app.war"]