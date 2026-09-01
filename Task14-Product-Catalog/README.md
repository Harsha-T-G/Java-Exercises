# Product Catalog API

Spring Boot REST API for product management. Week 5 delivered an in-memory
catalog; Week 6 adds PostgreSQL persistence with Flyway migrations.

**Status:** Week 6 Exercises 1–3 implemented (PostgreSQL, Flyway, JPA repository). Exercise 4+ pending.

## Prerequisites

- JDK 21
- Git
- Docker and Docker Compose (required for `./mvnw verify` — tests use PostgreSQL Testcontainers)

## Quick start (tests)

```bash
cd Task14-Product-Catalog
docker info   # Docker must be running
./mvnw clean verify
```

Tests spin up a PostgreSQL 16 container automatically via Testcontainers.

### Troubleshooting: Docker / Testcontainers errors

If tests fail with `Could not find a valid Docker environment` or
`ContainerFetch Can't get Docker image`:

1. **Start Docker Desktop** and confirm it is healthy:
   ```bash
   docker info
   docker pull postgres:16-alpine
   ```
2. **Fix global Testcontainers config** at `~/.testcontainers.properties`. Remove or comment out:
   ```properties
   docker.client.strategy=org.testcontainers.dockerclient.UnixSocketClientProviderStrategy
   ```
3. **Docker 29+ API version:** This project includes `src/test/resources/docker-java.properties`
   with `api.version=1.44` (required for Docker Desktop 29.x).
4. Run tests via the helper script:
   ```bash
   ./scripts/verify-tests.sh
   ```

## Quick start (application with PostgreSQL)

1. Copy environment template and set a local password:

```bash
cp .env.example .env
# Edit .env — set POSTGRES_PASSWORD and DB_PASSWORD (same value is fine locally)
```

2. Start PostgreSQL:

```bash
docker compose --env-file .env up -d
docker compose ps   # wait until postgres is healthy
```

3. Export database credentials and run the application:

```bash
set -a && source .env && set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
curl http://localhost:8080/api/info
curl http://localhost:8080/actuator/health
```

4. Stop PostgreSQL when finished:

```bash
docker compose --env-file .env down
```

To remove persisted data as well:

```bash
docker compose --env-file .env down -v
```

Default port: **8080**. PostgreSQL: **5432**.

### Required environment variables

| Variable | Purpose |
|----------|---------|
| `DB_URL` | JDBC URL (default in dev profile: `jdbc:postgresql://localhost:5432/product_catalog`) |
| `DB_USERNAME` | Application database user (default: `product_catalog_app`) |
| `DB_PASSWORD` | Application password — **never commit**; set via `.env` locally |
| `POSTGRES_PASSWORD` | Used by Docker Compose to initialize the container user |

If the database is unavailable, the application fails at startup with a clear
connection error. No credentials belong in Git — use `.env` (gitignored) or your
shell environment.

### Hibernate and Flyway

- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate never creates or alters tables.
- Flyway owns schema changes under `src/main/resources/db/migration/`.
- Migrations are added in Week 6 Exercise 2.

## Profiles

| Profile | Command | Database | low-stock threshold | max products |
|---------|---------|----------|---------------------|--------------|
| default | Requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL | 5 | 500 (or `CATALOG_MAXIMUM_PRODUCTS`) |
| dev | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` + `.env` | PostgreSQL (Docker Compose) | 10 | 1000 |
| test | `./mvnw test` (automatic via `src/test/resources/application.properties`) | PostgreSQL Testcontainers | 2 | 20 |

Override max products:

```bash
CATALOG_MAXIMUM_PRODUCTS=5 ./mvnw spring-boot:run
```

## Endpoint table

| Method | Path | Status | Purpose |
|--------|------|--------|---------|
| GET | `/api/info` | 200 | Application metadata |
| GET | `/actuator/health` | 200 | Health check |
| GET | `/actuator/info` | 200 | Build info |
| POST | `/api/products` | 201 | Create product |
| GET | `/api/products` | 200 | List all products |
| GET | `/api/products/low-stock` | 200 | Active low-stock products |
| GET | `/api/products/{id}` | 200 | Get one product |
| PUT | `/api/products/{id}` | 200 | Update product |
| DELETE | `/api/products/{id}` | 204 | Delete product |

Error responses: **400** validation, **404** not found, **405** method not allowed, **409** conflict, **500** unexpected.

## Sample requests

**Create product**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-001","name":"Sample","category":"General","price":19.99,"stockQuantity":10,"active":true}'
```

**Validation error (400)**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"","name":"X","category":"General","price":-1,"stockQuantity":0}'
```

See [docs/curl-commands.sh](docs/curl-commands.sh) for more samples.

## Package structure

```text
com.codewalnut.productcatalog/
├── controller/   InfoController, ProductController
├── service/      ProductService
├── repository/   ProductRepository (Spring Data JPA)
├── entity/       ProductEntity
├── dto/          Request/response and error payloads
├── mapper/       ProductEntityMapper
├── exception/    Domain exceptions, GlobalExceptionHandler
└── config/       CatalogProperties
```

## Exercise branches

```text
task14-main → exercise-1-setup → … → exercise-6-tests →
week6-exercise-1-postgresql-config → …
```

## Agentic workflow

| Artifact | Path |
|----------|------|
| Spec | [SPEC.md](SPEC.md) |
| Specs | [docs/specs/product-catalog/](docs/specs/product-catalog/) |
| Plans | [docs/plans/](docs/plans/) |
| Self review | [SELF_REVIEW.md](SELF_REVIEW.md) |
| Test evidence | [docs/test-evidence.txt](docs/test-evidence.txt) |

## Tests

```bash
./mvnw clean verify
./mvnw -Dtest=ProductServiceTest test
./mvnw -Dtest=ProductIntegrationTest test
```
