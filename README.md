# Spring Security & JWT

Демонстрационный проект аутентификации и авторизации на Spring Security с JWT-токенами. Показывает полный цикл: регистрация, логин, обновление access-токена по refresh-токену, выход с отзывом токена, ролевая модель доступа (USER/ADMIN).

## Возможности

- Регистрация и логин с выдачей пары токенов (access + refresh)
- Обновление access-токена по refresh-токену без повторного ввода пароля
- Logout с отзывом refresh-токена (хранится в БД, может быть удалён)
- Ролевая модель доступа: `USER`, `ADMIN`, управление ролями пользователей через отдельные эндпоинты
- Кастомные JSON-ответы на ошибки авторизации (401/403) вместо стандартных страниц Spring Security
- Security-заголовки (HSTS, CSP, X-Frame-Options, Cross-Origin-Resource-Policy)
- Дифференцированное кэширование ответов в зависимости от типа эндпоинта (публичный / защищённый / административный)

## Архитектура аутентификации

Используются **два JWT-токена с разными секретами**:

- **Access-токен** — короткоживущий, передаётся в заголовке `Authorization: Bearer <token>`, содержит роли пользователя в claims.
- **Refresh-токен** — долгоживущий, хранится в базе данных. Это позволяет отозвать его до истечения срока действия (при logout или компрометации), чего нельзя сделать с обычным stateless JWT.

Проверка access-токена выполняется в `JwtAuthenticationFilter`, который встроен в цепочку фильтров Spring Security до `UsernamePasswordAuthenticationFilter`. Ошибки токена (истёк, повреждён, неподдерживаемый формат) различаются через собственную иерархию исключений и возвращаются клиенту как структурированный JSON через `CustomAuthenticationEntryPoint`.

Сессии не хранятся на сервере (`SessionCreationPolicy.STATELESS`) — состояние авторизации полностью переносится в токене.

## Технологии

- Java, Spring Boot
- Spring Security
- Spring Data JPA
- JJWT (io.jsonwebtoken) для генерации и валидации токенов
- BCrypt для хранения паролей
- Lombok

## API

### Аутентификация `/api/auth`

| Метод | Endpoint | Доступ | Описание |
|---|---|---|---|
| POST | `/login` | публичный | Логин, возвращает access + refresh токены |
| POST | `/register` | публичный | Регистрация нового пользователя |
| POST | `/refresh` | публичный | Обновление access-токена по refresh-токену |
| POST | `/logout` | требует токен | Выход, отзыв refresh-токена |

### Пользователь `/user`

| Метод | Endpoint | Доступ | Описание |
|---|---|---|---|
| GET | `/` | USER, ADMIN | Получить данные текущего авторизованного пользователя |

### Администрирование `/admin`

| Метод | Endpoint | Доступ | Описание |
|---|---|---|---|
| GET | `/users` | ADMIN | Список всех пользователей |
| GET | `/users/{id}` | ADMIN | Получить пользователя по id |
| POST | `/users` | ADMIN | Создать пользователя |
| PUT | `/users/{id}` | ADMIN | Обновить пользователя |
| DELETE | `/users/{id}` | ADMIN | Удалить пользователя |
| PUT | `/users/{userId}/roles/{roleName}` | ADMIN | Назначить роль пользователю |
| DELETE | `/users/{userId}/roles/{roleName}` | ADMIN | Снять роль с пользователя |

## Примеры запросов

Логин:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}'
```

Запрос с access-токеном:
```bash
curl http://localhost:8080/user \
  -H "Authorization: Bearer <access_token>"
```

Обновление access-токена:
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<refresh_token>"}'
```

## Конфигурация

Параметры JWT задаются в `application.properties`:

```properties
jwt.accessSecret=<base64-encoded secret>
jwt.refreshSecret=<base64-encoded secret>
jwt.accessTokenExpiration=15
jwt.refreshTokenExpiration=10080
```

> Секреты должны быть достаточно длинными Base64-строками — они используются для подписи токенов алгоритмом HMAC-SHA512.

## Известные ограничения

Проект сделан для демонстрации механизмов Spring Security и JWT, а не как production-ready сервис. В частности:

- В `SecurityConfig` включено принудительное перенаправление на HTTPS (`requiresChannel().requiresSecure()`) — для локального запуска без SSL это нужно временно отключить или запускать через прокси с TLS-терминацией.
- CORS настроен с `allowedOriginPatterns("*")` в сочетании с `allowCredentials(true)` — допустимо для демонстрации, но для продакшена origin должны быть явно ограничены конкретными доменами.

## Запуск

```bash
./mvnw spring-boot:run
```

Приложению нужна настроенная база данных (см. `application.properties`) — таблицы пользователей, ролей и refresh-токенов создаются через JPA/миграции при старте.