# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
# data folder for H2 file database
VOLUME /app/data
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
