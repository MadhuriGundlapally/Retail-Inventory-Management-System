# Retail Inventory Management System

Full-stack inventory app: **Java 17, Spring Boot 3, Spring Data JPA, MySQL**, with a small web UI and a Postman collection.

## Features
- Layered design: `controller` (REST) → `service` (business rules) → `repository` (JPA) → `model` (entities), with DTO records for requests/responses.
- Products CRUD with unique SKU, category filter, bean validation.
- Stock tracking: receive/remove stock via `POST /api/stock/{productId}/adjust`; negative stock is rejected.
- Low-stock report: `GET /api/stock/low` (quantity ≤ reorder level).
- Centralised error handling → consistent JSON errors (400 validation, 404 not found, 409 duplicate SKU / insufficient stock).
- Web UI at `/` (served from `static/index.html`).

## API
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/products` | Create product (+ initial stock) |
| GET | `/api/products?category=` | List / filter |
| GET/PUT/DELETE | `/api/products/{id}` | Read / update / delete |
| GET | `/api/stock/{productId}` | Current stock |
| POST | `/api/stock/{productId}/adjust` | `{ "delta": -3, "reason": "sale" }` |
| GET | `/api/stock/low` | Items to reorder |

## Run with Docker (app + MySQL)
```bash
docker compose up --build      # http://localhost:8080
```

## Run locally
```bash
# needs MySQL running; override with DB_URL, DB_USER, DB_PASSWORD
mvn spring-boot:run
```

## Tests
```bash
mvn verify      # 14 tests (MockMvc integration on H2 + Mockito unit tests); no MySQL needed
```

## Postman
Import `postman/inventory.postman_collection.json`; run the collection in order. It includes success, 400, 404 and 409 cases with assertions.

## SQL
`sql/queries.sql` has reporting queries (low stock, stock value per category, out-of-stock).

## Deploy
Render / Railway / Fly.io: deploy the `Dockerfile`, attach a managed MySQL and set `DB_URL`, `DB_USER`, `DB_PASSWORD`. Set `DDL_AUTO=validate` once the schema exists, and add authentication before exposing it publicly.
