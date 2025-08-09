# Ports (with example enviroment varibles)

| Service                                         | Port  |
|-------------------------------------------------|-------|
| Frontend (reserved)                             | 37000 |
| Gateway API (reserved)                          | 37001 |
| Kafka LISTENERS AND KAFKA_LISTENERS (PLAINTEXT) | 9092  |
| Users service (REST API)                        | 37002 |
| Users service (Testing, REST API)               | 37032 |
| Users DB (Postgres)                             | 37012 |

# Environment variables

## For :bootRun

```
POSTGRES_LOGIN=postgres;POSTGRES_HOST=localhost;POSTGRES_PASSWORD=postgres;POSTGRES_PORT=37012;POSTGRES_DB=myblog;APP_PORT=37002
```

## For :test

```
TEST_PORT=37032
```