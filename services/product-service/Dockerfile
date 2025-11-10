FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY product-adapters-in-rest/target/product-adapters-in-rest-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
