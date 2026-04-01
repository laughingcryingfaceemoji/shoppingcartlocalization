FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY . /app
RUN mvn package -DskipTests

FROM eclipse-temurin:17
RUN apt-get update && apt-get install -y \
    xvfb \
    libgtk-3-0 \
    libxxf86vm1 \
    libxrender1 \
    libxtst6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/laskin.jar .

CMD ["bash", "-c", "Xvfb :99 -screen 0 1024x768x24 & export DISPLAY=:99 && java -jar laskin.jar"]