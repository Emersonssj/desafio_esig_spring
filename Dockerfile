# Etapa 1: Build (Compilação)
# Usando o Maven com Java 17 (se você usou o 21, mude aqui para eclipse-temurin-21)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia os arquivos de dependência primeiro (otimiza o cache do Docker)
COPY pom.xml .
COPY src ./src

# Compila o projeto ignorando os testes para ser mais rápido
RUN mvn clean package -DskipTests

# Etapa 2: Run (Execução)
# Usa uma imagem mais leve apenas com o Java para rodar a aplicação
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o arquivo .jar gerado na Etapa 1
COPY --from=build /app/target/*.jar app.jar

# Cria a pasta uploads dentro do container para evitar erros ao salvar imagens
RUN mkdir -p /app/uploads

# Expõe a porta que o Spring Boot vai rodar
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]