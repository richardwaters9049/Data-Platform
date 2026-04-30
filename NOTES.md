# Project Notes

## **30/04/2026**

---

### **01:29** - Backend ETL refactor completed

Moved CSV upload flow from controller to service-based architecture. `UploadController` delegates to `RecordImportService`, while `CsvRecordParser` handles parsing and validation. Responses now include row counts and validation errors.

`./mvnw test` passed with 3 tests, 0 failures.

---

### **01:36** - GitHub repository created

Initialized Git repository on `main` and pushed to `richardwaters9049/data-platform`.

---

### **01:39** - Local-only folders removed from Git tracking

Removed `Interview-info/` from GitHub, kept locally. Added `Interview-info/` and `YouTube/` to `.gitignore`.

---

### **01:50** - Project testing completed from terminal

Ran app on port `8081` (port `8080` was occupied). Tested health check and CSV upload via terminal. Valid rows inserted to PostgreSQL, invalid rows returned `400 Bad Request` with validation errors.

---

### **01:55** - Docker Compose file created

Added `docker-compose.yml` to define local infrastructure. It starts PostgreSQL 17 on `localhost:55432` with a `dataplatform` database, plus Redis 7 Alpine on `localhost:56379`. Both services have named Docker volumes for data persistence and health checks.

Spring configuration updated to override server port, database settings, Redis settings, and JPA logging/schema behavior with environment variables.

`docker compose config` passed, and `./mvnw test` passed with 3 tests and 0 failures.

---

### **02:07** - Vue frontend scaffold created

Added `frontend/` with Vue, Vite, Bun, Tailwind CSS, and Lucide icons. Built automotive data ingestion console with API health checking, CSV upload, import summaries, and validation display. Frontend uses Vite proxy to reach backend on `localhost:8081`.

Dependencies installed with Bun, `bun run build` successful.

---

### **02:14** - Frontend added to Docker Compose

Added `frontend/Dockerfile` using `oven/bun:1.3.1-alpine`. Compose includes `frontend` service on `localhost:5173`. Proxy target configurable via `VITE_API_PROXY_TARGET`, pointing to `host.docker.internal:8081`.

---

### **02:22** - Frontend, documentation, sample data, and comments prepared

Added professional README with architecture, tech stack, API docs, and setup guide. Added `samples/automotive-contacts.csv` for testing. Added light comments across codebase for navigation.

Validation: `./mvnw test`, `bun run build`, `docker compose config` - all passed.

---

### **02:25** - Full platform Docker command added

Added root `Dockerfile` for Spring Boot backend and `.dockerignore`. Compose now includes `backend` service with `frontend`, `postgres`, and `redis`. Full stack runs with `docker compose up --build`.

Backend connects to PostgreSQL via Compose service name. Frontend proxies to `http://backend:8080`. Exposed ports: backend `8081`, frontend `5173`.

Validation: `docker compose config`, `./mvnw test`, `bun run build`, `docker compose build` - all passed.

---

### **03:16** - Upload CSV button functionality fixed

Fixed Vite proxy chunked transfer encoding issue with multipart/form-data. Configured frontend to call backend directly at `http://localhost:8081/api/upload`. CSV upload now processes successfully.

---

### **03:36** - Package refactoring: com.dataplatform to com.platform

Renamed package from `com.dataplatform.*` to `com.platform.*` to eliminate redundancy with project name. Updated all 28 Java files, imports, and moved directory structure. Application builds and runs successfully.

---

### **03:38** - CORS configuration added for frontend-backend communication

Fixed "API not reachable" error by adding CORS configuration. Created `WebConfig.java` with CORS mapping for `/api/**` endpoints from `http://localhost:5173`. Backend rebuilt with CORS headers. CSV upload now works end-to-end.

---

### **03:54** - Automotive data platform refactoring completed

Transformed platform from basic contact records to comprehensive automotive data management.

**Added:**

- New entities: Vehicle, Dealer, Warranty, Fleet, ServiceRecord
- AutomotiveImportService for type-specific CSV imports with validation
- StatisticsService with Redis-backed caching (5-minute cache)
- AutomotiveController and RecordsController with new API endpoints
- Sample data files (vehicles.csv, dealers.csv)

**New API Endpoints:**

- `/api/automotive/upload/{dataType}` - Type-specific CSV uploads
- `/api/automotive/data-types` - Available data schemas
- `/api/records/{type}` - Paginated record listings
- `/api/records/statistics/*` - Redis-cached statistics

**Testing:**

- Sample imports working
- Redis caching operational
- All endpoints responding correctly

---

### **12:59** - Null type safety fixes completed

Fixed null type safety warnings across RecordsController and StatisticsService to improve code quality and eliminate compiler warnings.

**RecordsController Fixes:**

- Added explicit null checks for all `@PathVariable Long id` parameters
- Returns `400 Bad Request` for null IDs, maintaining existing `404 Not Found` for invalid IDs
- Updated methods: getVehicle(), getDealer(), getWarranty(), getFleet(), getService()

**StatisticsService Fixes:**

- Replaced `CACHE_DURATION` field usage with direct `Duration.ofMinutes(5)` calls
- Eliminated null type safety warnings for Duration parameters in Redis operations
- Maintained Redis caching functionality with 5-minute cache duration

**Benefits:**

- Eliminates all null type safety warnings
- Improves code robustness with explicit null validation
- Maintains existing functionality while improving type safety

---

## **Next Planned Step**

Refactor backend import schema for automotive data (vehicle, dealer, warranty, fleet, service records). Add Redis-backed summary/query behavior and expose endpoints for listing imported records and statistics.
