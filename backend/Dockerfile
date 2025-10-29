
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -B -DskipTests package


FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

# Install docker client for optional docker-based judge mode
RUN apt-get update \
	&& apt-get install -y --no-install-recommends ca-certificates curl gnupg \
	&& apt-get install -y --no-install-recommends docker.io \
	&& rm -rf /var/lib/apt/lists/*

VOLUME /app/data
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
