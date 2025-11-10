FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the aggregated JAR
COPY files-adapters-in-rest/target/files-adapters-in-rest-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
