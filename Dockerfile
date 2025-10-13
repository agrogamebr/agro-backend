# ===========================================
# STAGE 1: BUILD
# Usa Maven para compilar a aplicacao
# ===========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# Define diretorio de trabalho
WORKDIR /app

# Copia o pom.xml e baixa as dependencias (cacheable)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o codigo fonte
COPY src ./src

# Compila a aplicacao (pula os testes para build mais rapido)
RUN mvn clean package -DskipTests

# ===========================================
# STAGE 2: RUNTIME
# Usa JRE minima para rodar a aplicacao
# ===========================================
FROM eclipse-temurin:21-jre-alpine

# Define diretorio de trabalho
WORKDIR /app

# Cria usuario nao-root para seguranca
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o JAR compilado do stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Expoe a porta 8080 (padrao Spring Boot)
EXPOSE 8080

# Variaveis de ambiente para JVM otimizada
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando para rodar a aplicacao
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
