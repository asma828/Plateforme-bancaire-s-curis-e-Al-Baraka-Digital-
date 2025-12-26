# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .
COPY src ./src

# Construire l'application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S spring && adduser -S spring -G spring

# Copier le JAR depuis le stage de build
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory as root
RUN mkdir -p /app/uploads \
    && chown -R spring:spring /app

USER spring


# Exposer le port de l'application
EXPOSE 8080

# Variables d'environnement par défaut
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Démarrer l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]