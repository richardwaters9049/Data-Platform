# Project Notes

Date: 30/04/2026

01:29 - Backend ETL refactor completed.

The CSV upload flow was moved out of the controller and into a cleaner service-based structure. `UploadController` now delegates to `RecordImportService`, while `CsvRecordParser` handles row parsing and validation. Upload responses now return structured counts for rows read, imported, and rejected, plus row-level validation errors.

Tests were added for valid and invalid CSV imports. `./mvnw test` passed with 3 tests and 0 failures.

01:36 - GitHub repository created.

The project was initialized as a Git repository on `main`, committed, and pushed to GitHub at `richardwaters9049/data-platform`.

01:39 - Local-only folders removed from Git tracking.

`Interview-info/` was removed from the GitHub repository but kept locally. `Interview-info/` and `YouTube/` are now ignored by Git so local planning notes stay on this machine only.

01:50 - Project testing completed from the terminal.

The app was run on port `8081` because another Java process was already using `8080`. Health check and CSV upload were tested with terminal commands. Valid CSV rows inserted into PostgreSQL successfully. Invalid CSV rows returned `400 Bad Request` with validation errors for missing name, invalid email, and invalid age.

01:55 - Docker Compose file created.

Added `docker-compose.yml` to define the local infrastructure for the project. It starts PostgreSQL 17 on `localhost:55432` with a `dataplatform` database, plus Redis 7 Alpine on `localhost:56379`. Both services have named Docker volumes so data can persist between restarts, and both include health checks so their readiness can be inspected with Docker.

Spring configuration was updated so server port, database settings, Redis settings, and JPA logging/schema behavior can be overridden with environment variables. This keeps the local setup simple while making the app easier to adapt later for Docker, CI, and Azure.

`docker compose config` passed, and `./mvnw test` passed with 3 tests and 0 failures.

02:07 - Vue frontend scaffold created.

Added a `frontend/` application using Vue, Vite, Bun, Tailwind CSS, and Lucide icons. The first screen is an automotive data ingestion console with API health checking, CSV upload, import summary cards, validation error display, and pipeline status indicators. The frontend uses a Vite proxy so `/api` and `/health` requests can reach the Spring Boot backend running on `localhost:8081`.

Dependencies were installed with Bun, and `bun run build` completed successfully.

02:14 - Frontend added to Docker Compose.

Added `frontend/Dockerfile` using the `oven/bun:1.3.1-alpine` image. Docker Compose now includes a `frontend` service that builds the Vue application container and exposes it on `localhost:5173`. The frontend proxy target is configurable with `VITE_API_PROXY_TARGET`, and the Compose service points it at `host.docker.internal:8081` so it can reach the Spring Boot backend running from the local terminal.

02:22 - Frontend, documentation, sample data, and comments prepared for commit.

The project now has a professional README that explains the automotive data platform, architecture, tech stack, expected API output, local setup, and roadmap. A dummy CSV file was added at `samples/automotive-contacts.csv` for terminal and frontend upload testing. Light comments were added across the backend, frontend, and Docker Compose files to make the code easier to navigate without over-documenting obvious lines.

Validation was run again with `./mvnw test`, `bun run build`, and `docker compose config`; all passed.

Next planned step:

Refactor the backend import schema towards automotive data such as vehicle, dealer, warranty, fleet, or service records. Then add Redis-backed summary/query behaviour and expose a simple endpoint to list imported records or return import statistics.
