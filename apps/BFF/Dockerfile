# Use Java 21
FROM eclipse-temurin:21-jdk

# Instal Maven
RUN apt-get update && \
    apt-get install -y maven

# Copy the pom.xml and source code
COPY pom.xml .
COPY src /src

# Compile Maven project
RUN mvn clean package -DskipTests

# Copie o jar gerado pelo Maven
COPY target/BFF-1.0-SNAPSHOT.jar app.jar

# Exponha a porta usada pela aplicação
EXPOSE 8981

# Comando para rodar o app
ENTRYPOINT ["java", "-jar", "app.jar"]