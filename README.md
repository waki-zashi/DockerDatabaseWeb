# Phonebook

**Phonebook** — это полноценное веб-приложение для управления телефонной книгой с акцентом на **защиту от "ошибок на дурака"**, строгую валидацию данных и надёжную обработку ошибок на всех уровнях.

Проект демонстрирует многоуровневую архитектуру с валидацией на фронтенде, бэкенде, JPA и **на уровне базы данных PostgreSQL** (через CHECK-констрейнты и триггеры PL/pgSQL).

## Основные особенности проекта

- **Многоуровневая валидация данных**:
  - Frontend (JavaScript) — базовые проверки + защита от XSS
  - DTO + Bean Validation (`@Valid`, `@NotBlank`, `@Size`, `@Pattern`)
  - JPA-сущность (`@NotBlank`, `@Size`, `@Pattern` + автоматический trim)
  - База данных — **строгие CHECK + триггер** с кастомными английскими сообщениями об ошибках
- **Глобальная обработка исключений** (`@RestControllerAdvice`) — единый формат ошибок для клиента
- **Docker + docker-compose** — PostgreSQL + pgAdmin + приложение в контейнерах
- **REST API** с полным CRUD + поиск
- **Простой, но современный фронтенд** (HTML + CSS + vanilla JS)
- **Логирование** через Logback (консоль + файл с ротацией)
- **Безопасность**: экранирование HTML, защита от SQL-инъекций (JPA), ограничение длины, таймауты запросов

## Архитектура

```
Клиент (браузер) ── HTTP/REST ── Spring Boot (Controller → Service → Repository)
                                                        │
                                                        ▼
                                             PostgreSQL + триггеры / CHECK
```

### Backend слои

- **Controller** (`ContactController`) — маршрутизация, минимальные проверки
- **Service** (`ContactService`) — бизнес-логика, маппинг DTO → Entity, экранирование
- **Repository** (`ContactRepository`) — Spring Data JPA + кастомный JPQL-поиск
- **Model** (`Contact`) — JPA-сущность с валидацией и lifecycle-методами (`@PrePersist`, `@PreUpdate`)
- **DTO** (`ContactDto`) — входные/выходные данные с аннотациями валидации
- **Exception Handling** (`GlobalExceptionHandler`) — централизованная обработка всех исключений

### Frontend

- `index.html` + `style.css` + `script.js`
- Vanilla JS + fetch API
- Карточки контактов, форма, поиск, уведомления, лоадер

### База данных

Таблица `contacts` с полями:

- `id` SERIAL PRIMARY KEY
- `full_name` VARCHAR(100) NOT NULL
- `phone_number` VARCHAR(20) NOT NULL
- `note` VARCHAR(500)
- `created_at`, `updated_at` TIMESTAMP

**Защита на уровне БД** (файл `init.sql`):

- `NOT NULL` + ограничение длины
- CHECK-констрейнты (формат, символы)
- **Триггер BEFORE INSERT/UPDATE** → функция `validate_contact()` кидает `RAISE EXCEPTION` с понятным английским сообщением

## Обработка исключений

Проект реализует **многоуровневую и централизованную** обработку ошибок:

### 1. Уровень БД (самый строгий)

- Триггер PL/pgSQL возвращает понятные сообщения:
  - "Full name cannot be empty or consist only of spaces"
  - "Phone number can only contain digits, +, (, ), space and hyphen"
  - "Note cannot exceed 500 characters"
- Эти ошибки доходят до Hibernate → выбрасываются как `PSQLException` → ловятся в GlobalExceptionHandler

### 2. Уровень Spring Boot

- `@Valid` + Bean Validation → `MethodArgumentNotValidException`
- Некорректный JSON → `HttpMessageNotReadableException`
- Неверный тип параметра (`/api/contacts/abc`) → `MethodArgumentTypeMismatchException`
- Кастомное исключение `ContactNotFoundException` → 404
- `IllegalArgumentException` (невалидный id) → 400
- Все остальные → 500 с минимальной информацией

**GlobalExceptionHandler** возвращает единый JSON-формат:

```json
// Валидация поля
{ "fullName": "Name must contain from 2 to 100 characters" }

// Не найден
{ "error": "Contact not found with id: 999" }

// Общая ошибка
{ "error": "Internal server error", "details": "..." }
```

### 3. Уровень фронтенда

- Проверки перед отправкой (обязательные поля, длина)
- `sanitizeInput()` + `escapeHtml()` — защита от XSS
- Обработка HTTP-ошибок + таймаут 8 сек
- Уведомления (success / error / info) + лоадер

## Технологический стек

- **Backend**: Java 21, Spring Boot 3.2.0, Spring Data JPA, Hibernate, Jakarta Validation
- **БД**: PostgreSQL 15 + PL/pgSQL триггеры
- **Контейнеризация**: Docker, docker-compose
- **Логирование**: Logback (консоль + rolling file)
- **Frontend**: HTML5, CSS3 (градиенты, flex/grid, анимации), Vanilla JavaScript (fetch)
- **Сборка**: Maven

## Как запустить проект

### Вариант 1 — Полностью через Docker (рекомендуется)

1. Убедитесь, что установлен **Docker** и **Docker Compose**
2. Создайте (или используйте существующий) файл `.env` в корне проекта:

```env
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin123
PGADMIN_DEFAULT_EMAIL=admin@phonebook.com
PGADMIN_DEFAULT_PASSWORD=admin
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin123
```

3. Запустите:

```bash
docker compose up -d --build
```

4. Дождитесь запуска всех контейнеров (~30–60 сек)

Готово! Открывайте:

- Приложение: http://localhost:8080
- pgAdmin: http://localhost:5050  

### Вариант 2 — Локально (без Docker)

1. Запустите PostgreSQL (например через Docker):

```bash
docker run -d --name phonebook-postgres \
  -e POSTGRES_DB=phonebook \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin123 \
  -p 5432:5432 \
  -v ./init.sql:/docker-entrypoint-initdb.d/init.sql \
  postgres:15
```

2. Соберите проект:

```bash
mvn clean package
```

3. Запустите приложение:

```bash
java -jar target/phonebook-1.0.0.jar
# или
mvn spring-boot:run
```

4. Откройте http://localhost:8080

```

## Полезные команды

```bash
# Пересобрать и запустить
docker compose down && docker compose up -d --build

# Логи приложения
docker compose logs -f app

# Логи базы
docker compose logs -f postgres

# Зайти в контейнер postgres
docker compose exec postgres psql -U admin -d phonebook

# Очистить volume (если нужно сбросить БД)
docker compose down -v
```

## Безопасность

- Нельзя вставить некорректные данные даже через pgAdmin / прямой SQL — триггер БД заблокирует
- Все ошибки возвращаются в понятном JSON-формате
- Защита от XSS (escapeHtml на фронте + HtmlUtils на бэкенде)
- Таймауты и обработка сетевых ошибок на фронте
- Логирование всех важных операций и ошибок
