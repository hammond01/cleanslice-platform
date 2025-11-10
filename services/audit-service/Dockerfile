FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY audit-adapters-in-rest/target/audit-adapters-in-rest-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
