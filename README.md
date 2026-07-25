# LogSentinel

LogSentinel ingests application log files, publishes structured events to Kafka, evaluates alert rules, stores alerts in PostgreSQL, and exposes them through a REST API.

## Local development tools

Start the core stack after building the three Jib images:

```bash
docker compose up -d
```

Start the core stack together with Kafka UI and Adminer:

```bash
docker compose --profile tools up -d
```

| Tool | URL | Purpose |
|---|---|---|
| REST API | http://localhost:8080/alerts | View stored alerts as JSON |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | Explore and call the REST API |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | View the generated OpenAPI document |
| Kafka UI | http://localhost:8081 | Inspect topics, messages, partitions, and consumer groups (`tools` profile) |
| Adminer | http://localhost:8082 | Inspect PostgreSQL tables and run SQL queries (`tools` profile) |

### Kafka UI

Open the `log-sentinel` cluster, then inspect:

```text
Topics → log-events → Messages
```

The evaluator consumer group is:

```text
log-sentinel-evaluator
```

### Adminer

Use these local-development connection values:

| Field | Value |
|---|---|
| System | PostgreSQL |
| Server | `postgres` |
| Username | `log_sentinel` |
| Password | `log_sentinel` |
| Database | `log_sentinel` |

Example query:

```sql
SELECT
    id,
    rule_id,
    component,
    triggered_at,
    message,
    level
FROM alerts
ORDER BY triggered_at DESC;
```

These tools are intended for local development and do not have production authentication configured.
