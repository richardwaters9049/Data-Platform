# Project Notes

## 2026-04-30

Current status:

- GitHub repository is set up at `richardwaters9049/data-platform`.
- `main` is synced with `origin/main`.
- `YouTube/` planning notes are also kept locally and ignored by Git.
- Backend is a Java 17 Spring Boot project using Maven.
- PostgreSQL is configured through Spring Data JPA at `localhost:55432/dataplatform`.
- Current API includes:
  - `GET /health` for a simple health check.
  - `POST /api/upload` for CSV uploads.
- CSV import flow has been refactored into:
  - controller layer
  - import service layer
  - CSV parser/validator
  - repository persistence
- Upload results now return structured counts and row-level validation errors.
- Tests currently pass with `./mvnw test`.

Recent backend improvement:

- Moved CSV processing out of `UploadController`.
- Added `RecordImportService`.
- Added `CsvRecordParser`.
- Added `ImportResult` and `ImportError` DTOs.
- Added service-level tests for valid and invalid CSV imports.

Next planned step:

- Add Docker Compose for PostgreSQL and Redis.
- Then add Redis-backed summary/query behavior.
