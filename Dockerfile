# == ETAPA 1: Construcción del JAR con Maven==
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
ARG GITHUB_TOKEN
RUN mkdir -p /root/.m2 && echo "<settings><servers><server><id>github</id><username>x</username>\
<password>${GITHUB_TOKEN}</password></server></servers></settings>" > /root/.m2/settings.xml
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# == ETAPA 2: Configuración de la app Java ==
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
