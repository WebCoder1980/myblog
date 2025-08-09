# Ports (with example environment variables)

| Service                                         | Port  | Environment variable |
|-------------------------------------------------|-------|----------------------|
| Frontend (reserved)                             | 37001 | -                    |
| Gateway API (reserved)                          | 37002 | -                    |
| Kafka LISTENERS AND KAFKA_LISTENERS (PLAINTEXT) | 37003 | -                    |
| Kafka GUI                                       | 37004 | -                    |
| Users service (REST API)                        | 37011 | APP_PORT             |
| Users service (Testing, REST API)               | 37012 | TEST_PORT            |
| Users DB (Postgres)                             | 37013 | POSTGRES_DB          |

# Environment variables

## For :bootRun

```
POSTGRES_LOGIN=postgres;POSTGRES_HOST=localhost;POSTGRES_PASSWORD=postgres;POSTGRES_PORT=37013;POSTGRES_DB=myblog;APP_PORT=37011
```

## For :test

```
TEST_PORT=37012
```