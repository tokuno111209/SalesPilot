
# Java 17とMavenでアプリをビルド
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

# Java 17でアプリを実行
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# デモ環境を有効化
ENV SPRING_PROFILES_ACTIVE=demo

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
