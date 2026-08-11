# Etapa 1: Build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copia os arquivos de configuração do Maven e as dependências
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

# Baixa as dependências offline (opcional, acelera builds subsequentes)
RUN ./mvnw dependency:go-offline || true

# Copia o código-fonte e faz o build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia o .jar gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que o Spring Boot usa
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
