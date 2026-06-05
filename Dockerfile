FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY education-platform-backend ./education-platform-backend

WORKDIR /app/education-platform-backend

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/education-platform-backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]