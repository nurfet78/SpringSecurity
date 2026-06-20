# Spring Security & JWT

Демонстрационный проект аутентификации и авторизации на Spring Security с JWT-токенами. Показывает полный цикл: регистрация, логин, обновление access-токена по refresh-токену, выход с отзывом токена, ролевая модель доступа (USER/ADMIN) с валидацией по фиксированному набору ролей, самостоятельное управление профилем пользователем.

## Возможности

- Регистрация и логин с выдачей пары токенов (access + refresh)
- Обновление access-токена по refresh-токену без повторного ввода пароля
- Logout с отзывом refresh-токена текущего пользователя (определяется из токена, а не из тела запроса)
- Пользователь может посмотреть и обновить свой профиль, сменить пароль — без доступа к управлению ролями
- Администратор управляет всеми пользователями: создание, обновление, удаление, назначение и снятие ролей
- Роли ограничены фиксированным набором (`RoleName`) — присвоить произвольную несуществующую роль через API нельзя
- Защита от занятых username — проверка на конфликт при регистрации и обновлении профиля
- Кастомные JSON-ответы на ошибки авторизации (401/403) вместо стандартных страниц Spring Security
- Security-заголовки (HSTS, CSP, X-Frame-Options, Cross-Origin-Resource-Policy)
- Дифференцированное кэширование ответов в зависимости от типа эндпоинта (публичный / защищённый / административный)

## Архитектура аутентификации

Используются **два JWT-токена с разными секретами**:

- **Access-токен** — короткоживущий, передаётся в заголовке `Authorization: Bearer <token>`, содержит роли пользователя в claims.
- **Refresh-токен** — долгоживущий, хранится в базе данных. Это позволяет отозвать его до истечения срока действия (при logout или компрометации), чего нельзя сделать с обычным stateless JWT.

Проверка access-токена выполняется в `JwtAuthenticationFilter`, который встроен в цепочку фильтров Spring Security до `UsernamePasswordAuthenticationFilter`. Ошибки токена (истёк, повреждён, неподдерживаемый формат) различаются через собственную иерархию исключений и возвращаются клиенту как структурированный JSON через `CustomAuthenticationEntryPoint`.

Logout определяет пользователя из аутентификации в `SecurityContextHolder`, а не из тела запроса — это исключает возможность завершить чужую сессию, подставив произвольный username.

Сессии не хранятся на сервере (`SessionCreationPolicy.STATELESS`) — состояние авторизации полностью переносится в токене.

## Разделение прав: self-service vs администрирование

В системе два разных набора эндпоинтов с разной ответственностью:

- **`/user`** — действия от своего имени. Пользователь видит и редактирует только свой профиль и пароль. Изменение ролей через этот путь невозможно — `UpdateUserDto.roles` принудительно игнорируется на уровне контроллера.
- **`/admin/users`** — административные действия над любым пользователем, включая управление ролями. Доступно только роли `ADMIN`, проверка идёт через `hasAuthority(RoleName.ROLE_ADMIN.name())`.

Такое разделение исключает эскалацию привилегий через редактирование собственного профиля.

## Ролевая модель

Допустимые роли заданы перечислением `RoleName` (`ROLE_USER`, `ROLE_ADMIN`) — это единая точка истины, на которую ссылаются:

- `SecurityConfig` — проверка прав доступа к эндпоинтам;
- `DataInitializer` — создание дефолтных ролей при старте приложения;
- `UserServiceImpl` — валидация имени роли перед назначением пользователю.

Попытка назначить пользователю роль, не входящую в `RoleName` (например, опечатку или произвольную строку через `PUT /admin/users/{userId}/roles/{roleName}`), завершается ошибкой `400 Bad Request`, а не созданием новой записи в БД. Это исключает рассинхрон между ролями, которые можно создать через API, и ролями, которые реально проверяются в `SecurityConfig`.

Обновление пользователя с явно пустым набором ролей (`"roles": []`) также отклоняется — у пользователя должна остаться хотя бы одна роль.

## Технологии

- Java 21, Spring Boot
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
| POST | `/logout` | требует токен | Выход, отзыв refresh-токена текущего пользователя |

### Профиль пользователя `/user`

| Метод | Endpoint | Доступ | Описание |
|---|---|---|---|
| GET | `/` | USER, ADMIN | Получить данные текущего авторизованного пользователя |
| PUT | `/` | USER, ADMIN | Обновить свой профиль (роли изменить нельзя) |
| PATCH | `/password` | USER, ADMIN | Сменить свой пароль |

### Администрирование `/admin`

| Метод | Endpoint | Доступ | Описание |
|---|---|---|---|
| GET | `/users` | ADMIN | Список всех пользователей |
| GET | `/users/{id}` | ADMIN | Получить пользователя по id |
| POST | `/users` | ADMIN | Создать пользователя |
| PUT | `/users/{id}` | ADMIN | Обновить пользователя (включая роли) |
| DELETE | `/users/{id}` | ADMIN | Удалить пользователя |
| PUT | `/users/{userId}/roles/{roleName}` | ADMIN | Назначить роль пользователю (только из `RoleName`) |
| DELETE | `/users/{userId}/roles/{roleName}` | ADMIN | Снять роль с пользователя |

## Примеры запросов

Логин:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}'
```

Регистрация:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Test", "lastName": "User", "email": "test@example.com", "username": "testuser", "password": "pass123"}'
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

Logout (текущий пользователь определяется по токену):
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <access_token>"
```

Смена своего пароля:
```bash
curl -X PATCH http://localhost:8080/user/password \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword": "old123", "newPassword": "new456", "confirmPassword": "new456"}'
```

Создание пользователя администратором:
```bash
curl -X POST http://localhost:8080/admin/users \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Test", "lastName": "User", "email": "test@example.com", "username": "testuser", "password": "pass123"}'
```

Назначение роли пользователю:
```bash
curl -X PUT http://localhost:8080/admin/users/1/roles/ROLE_ADMIN \
  -H "Authorization: Bearer <admin_access_token>"
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

Приложению нужна настроенная база данных (см. `application.properties`) — таблицы пользователей, ролей и refresh-токенов создаются через JPA/миграции при старте. При первом запуске `DataInitializer` создаёт роли `ROLE_USER`/`ROLE_ADMIN` и тестовых пользователей.