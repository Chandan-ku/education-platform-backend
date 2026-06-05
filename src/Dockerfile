FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY bottelx ./collegenet

WORKDIR /app/collegenet

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/collegenet/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]