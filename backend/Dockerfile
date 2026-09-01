# ---------- Étape 1 : compilation ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Étape 2 : exécution ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV PORT=8091
EXPOSE 8091
# Render injecte la variable PORT au runtime (l'application écoute dessus via ${PORT}).
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+UseG1GC", "-jar", "app.jar"]