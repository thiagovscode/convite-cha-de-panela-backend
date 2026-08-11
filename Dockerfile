# Etapa 1: Build usando imagem oficial do Maven
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom.xml e faz o download das dependências (acelera os próximos builds)
COPY pom.xml .
RUN mvn dependency:go-offline || true

# Copia o código-fonte
COPY src ./src

# Executa o build sem rodar os testes
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia o .jar gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que o Spring Boot usa
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
