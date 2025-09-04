# Используем официальный образ OpenJDK 21
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию в контейнере
WORKDIR /app

# Копируем JAR файл из директории сборки в контейнер
COPY build/libs/*.jar app.jar

EXPOSE 8089

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
