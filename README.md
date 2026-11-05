# Minimart API

Spring Boot REST API for managing provinces, branches, stores, and store
whitelists. MySQL and the application can be started together with Docker
Compose.

## Requirements

- Docker Desktop with Docker Compose
- Git

Java 17 and Maven are only required when running the application or tests
directly on the host. The Docker build uses Java 17 and the Maven Wrapper.

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build
```

The services are available at:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- MySQL from the host: `localhost:3308`

The application container connects to MySQL through the Compose service name
`mysql` on port `3306`. The host port `3308` is only for external database
access.

The first startup creates the schema and loads the seed data from
`src/main/resources/schema.sql` and `src/main/resources/data.sql`.

To run in the background:

```bash
docker compose up --build -d
```

To view application logs:

```bash
docker compose logs -f app
```

To stop the services:

```bash
docker compose down
```

To stop the services and remove the persisted MySQL volume:

```bash
docker compose down -v
```

Use `down -v` when a completely fresh database is required. This removes all
data stored in the Compose volume.

## Seed credentials

The seed data creates the following account:

```text
Username: Superadmin
Password: @technicaltest_2026
```

The seeded account has the permissions required to access the protected
province, branch, store, and whitelist APIs.

## Authentication

Login is public. Other API operations require the access token returned by
the login endpoint.

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"Superadmin\",\"password\":\"@technicaltest_2026\"}"
```

Use the returned access token as a Bearer token:

```bash
curl http://localhost:8080/api/v1/province \
  -H "Authorization: Bearer <access-token>"
```

Refresh and logout endpoints:

- Refresh the access token by sending the refresh token in the request body:

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d "{\"refresh_token\":\"<refresh-token>\"}"
```

- Use the returned token in the request header, for example:

```bash
curl http://localhost:8080/api/v1/province \
  -H "Authorization: Bearer <access-token>"
```

- `POST /api/v1/auth/refresh-token`
- `GET /api/v1/auth/logout`

## Main endpoints

All endpoints below require authentication and the corresponding permission.

| Resource | Endpoints |
|---|---|
| Province | `GET /api/v1/province`, `GET /api/v1/province/{id}` |
| Branch | `GET /api/v1/branch`, `GET /api/v1/branch/{id}`, `POST /api/v1/branch`, `PUT /api/v1/branch/{id}`, `PATCH /api/v1/branch/delete-branch/{id}` |
| Store | `GET /api/v1/store`, `GET /api/v1/store/{id}`, `POST /api/v1/store`, `PUT /api/v1/store/{id}`, `PATCH /api/v1/store/delete-store/{id}` |
| Whitelist | `GET /api/v1/whitelist`, `GET /api/v1/whitelist/{id}`, `POST /api/v1/whitelist`, `DELETE /api/v1/whitelist/{id}` |

Store search supports pagination and province filtering:

```text
GET /api/v1/store?pageNumber=1&pageSize=10&provinceName=JAKARTA
```

The optional `isActive` parameter supports:

- `true`: active records only
- `false`: inactive records only
- omitted: both statuses, subject to visibility and soft-delete rules

An active whitelisted store remains visible when searching another province,
according to the whitelist business rule.

## Audit trail

Write requests to resource endpoints under `/api/v1/` are recorded in the
`audit_logs` table:

- `POST` as `CREATE`
- `PUT` and regular `PATCH` as `UPDATE`
- `DELETE` and delete-style `PATCH` as `DELETE`

Audit records include the actor, resource, HTTP method, request path, resource
ID, request ID, status, outcome, client IP, user agent, error type, duration,
and timestamp. Authentication endpoints are excluded from this resource audit
filter. Passwords, JWTs, and request bodies are not stored.

## Run tests

Start MySQL first so the Spring context test can connect to the configured
database:

```bash
docker compose up -d mysql
```

Run the complete test suite using the Maven Wrapper:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Run only the service unit tests:

```powershell
.\mvnw.cmd "-Dtest=BranchServiceImplTest,StoreServiceImplTest,StoreWhitelistServiceImplTest,AuditLogServiceImplTest" test
```

The tests use Mockito for service-level unit tests. The Spring context test
requires a reachable MySQL instance using the configured datasource settings.

## Run without Docker

Start a compatible MySQL instance on `localhost:3308`, then run:

```powershell
.\mvnw.cmd spring-boot:run
```

Alternatively, build and run the packaged application:

```powershell
.\mvnw.cmd clean package
java -jar target\api-0.0.1-SNAPSHOT.jar
```

For non-Docker environments, configure the datasource and JWT settings through
environment variables or `application.properties`. Never use the development
credentials and fallback JWT secrets in production.

## Troubleshooting

### Port already in use

Stop the process using port `8080` or `3308`, or change the corresponding host
port mapping in `docker-compose.yaml`.

### Database starts but the app cannot connect

Check service health and application logs:

```bash
docker compose ps
docker compose logs mysql
docker compose logs app
```

The app must use `mysql:3306` from inside Docker, not `localhost:3308`.

### Seed data is not reloaded

MySQL data persists in the `mysql-data` volume. Recreate it when a clean seed
is needed:

```bash
docker compose down -v
docker compose up --build
```

