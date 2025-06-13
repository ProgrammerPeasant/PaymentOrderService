# PaymentOrderService

**PaymentOrderService** — это сервис для управления платёжами и соаздания заказов, реализованный на Java 21 с использованием Spring для внедрения зависимостей и сборкой через Gradle.

## Описание

Данный проект предназначен для обработки, хранения и управления платёжными поручениями в рамках микросервисной архитектуры.

Также простенький, кривой фронтенд с уведомлениями по вебсокету об оплате (там все очень минималистично)

## Особенности

- **Язык:** Java 21
- **DI:** Spring Framework
- **Сборка:** Gradle Kotlin DSL
- REST API для управления платёжными поручениями

## Запуск

### Требования

- Java 21+
- Gradle 8+
- Docker
- Node.js вместе с npm (для фронтенда)

### Сборка и запуск

```bash
git clone https://github.com/ProgrammerPeasant/PaymentOrderService.git
cd PaymentOrderService
./gradlew build или gradle build # докеры однофазные поэтому надо сначала билдить проект
cd frontend
npm install
docker-compose up
```
