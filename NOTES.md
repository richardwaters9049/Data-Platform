# Project Notes

## **10/05/2026**

---

### **00:52** - Current project status and latest technical changes

The project is now an automotive ETL data platform with a Spring Boot backend, Vue/Tailwind frontend, PostgreSQL persistence, Redis-backed statistics caching, Docker Compose orchestration, and sample automotive CSV files for vehicles and dealers. The application has moved beyond a generic CSV uploader and now demonstrates a clearer industry-style data workflow: ingest automotive CSV data, validate rows, transform values, persist accepted records, expose API endpoints, and serve operational data to the frontend.

Current architecture includes explicit ETL stages:

- `CsvIngestionService` for file reading and row extraction
- `AutomotiveDataValidator` for business rule and data quality checks
- `AutomotiveDataTransformer` for normalisation and entity mapping
- `AutomotivePipelineService` for orchestration, persistence, and cache invalidation
- PostgreSQL for durable storage
- Redis for cached statistics
- Docker Compose for running the full local platform

Latest technical changes add relationship mapping during transformation. Vehicle imports can now carry a dealer code, warranty imports can carry a VIN, and service record imports can carry both VIN and dealer code so the persistence layer can connect records to existing vehicles and dealers where possible. Application configuration was also adjusted for Redis and Hibernate PostgreSQL dialect settings.

Review follow-up needed before committing as final: unresolved dealer/vehicle relationship placeholders should be cleared when a matching existing entity is not found, otherwise JPA may fail on transient related objects. The local Redis default should also stay aligned with the Compose host port when running the backend outside Docker.

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

### **15:21** - ETL Pipeline Architecture Refactoring Completed

Refactored Spring Boot service layer to implement clear ETL pipeline architecture with explicit separation of concerns and interview-ready structure.

**New Package Structure:**

```
service/
ingestion/CsvIngestionService.java          # CSV file reading & raw data extraction
validation/AutomotiveDataValidator.java      # Business rules & data validation
transformation/AutomotiveDataTransformer.java # Data normalization & derived fields
pipeline/AutomotivePipelineService.java      # ETL orchestrator
```

**Key Improvements:**

- **Clear Pipeline Stages**: ingestion -> validation -> transformation -> persistence -> cache invalidation
- **Separation of Concerns**: Each service has single responsibility
- **Method Naming**: `runPipeline()`, `validateVehicle()`, `transformVehicle()` for clarity
- **Validation vs Transformation**: Explicit separation - validation checks data, transformation modifies valid data only
- **Backward Compatibility**: Legacy `AutomotiveImportService` (deprecated) delegates to new pipeline
- **API Endpoints Unchanged**: `/api/automotive/upload/{dataType}` works identically

**Pipeline Flow:**

1. **INGESTION**: `CsvIngestionService.ingestCsv()` - Read CSV files and extract raw data
2. **VALIDATION**: `AutomotiveDataValidator.validate*()` - Apply business rules and constraints
3. **TRANSFORMATION**: `AutomotiveDataTransformer.transform*()` - Normalize data and calculate derived fields
4. **PERSISTENCE**: Repository operations - Store to PostgreSQL with relationship handling
5. **CACHE INVALIDATION**: Clear Redis statistics cache for fresh data

**Benefits:**

- **Interview-Ready Architecture**: Clear ETL pattern demonstration
- **Maintainability**: Each component has single, clear responsibility
- **Testability**: Individual pipeline stages can be tested in isolation
- **Scalability**: Pipeline can be extended with new stages or data types
- **Production Features**: Cache invalidation, error handling, partial success processing

**Verification:**

- **Build**: `./mvnw clean compile` - SUCCESS (32 source files)
- **API Compatibility**: All endpoints unchanged
- **Functionality**: Same behavior with improved structure

---

### **15:45** - Enhanced Validation Error Reporting Completed

Implemented comprehensive validation error reporting with exact CSV location details to help users identify and fix data issues more effectively.

**Enhanced ImportError DTO:**

- Added `columnName` field for readable column names (e.g., "VIN" instead of "vin")
- Added `csvLine` field for complete CSV line content
- Maintained backward compatibility with existing constructors
- Added helper methods `withColumn()` and `withLine()` for enhanced error creation

**Detailed Validation Messages:**

- **Before**: `"Year must be between 1900 and 2100"`
- **After**: `"Year '1850' must be between 1900 and 2100"`

**Comprehensive Error Context:**

- Row number for exact line location
- Column name for human-readable field identification
- Specific problematic values in error messages
- Complete CSV line content for debugging

**Example Enhanced Error:**

```json
{
  "rowNumber": 5,
  "field": "year",
  "message": "Year '1850' must be between 1900 and 2100",
  "columnName": "Year",
  "csvLine": "1HGCM82633A123456,Honda,Civic,1850,EX,Blue,Gas,AUTO,1.8,Sedan,D001,ACTIVE"
}
```

**Files Updated:**

- `ImportError.java` - Enhanced DTO with new fields and helper methods
- `AutomotiveDataValidator.java` - All validation methods with detailed reporting
- `AutomotivePipelineService.java` - CSV structure validation with column context

**Benefits:**

- Users can pinpoint exact error locations in CSV files
- Specific problematic values shown in error messages
- Human-readable column names improve usability
- Complete CSV line context for efficient debugging
- Backward compatible with existing integrations

**Verification:**

- Build successful with 32 source files
- All validation methods enhanced with detailed error reporting
- Ready for improved user experience in CSV data validation

---

### **17:02** - Hot Reloading Configuration Added to Docker Setup

Implemented hot reloading for both backend and frontend to eliminate the need for manual container restarts during development.

**Development Dockerfiles Created:**

- `Dockerfile.dev` - Backend development Dockerfile using Maven with Spring Boot DevTools
- `frontend/Dockerfile.dev` - Frontend development Dockerfile using Vite with HMR enabled

**Backend Hot Reloading Features:**

- Spring Boot DevTools enabled for automatic application restart
- LiveReload on port 35729 for automatic browser refresh
- Java debugging on port 5005 for IDE integration
- Source code mounted as volume for instant code updates
- Maven `spring-boot:run` command for development mode

**Frontend Hot Reloading Features:**

- Vite HMR (Hot Module Replacement) for instant frontend updates
- WebSocket on port 24678 for live frontend reloading
- Source code mounted as volume for instant code updates
- Vite dev server running with `--host 0.0.0.0` for Docker networking

**Docker Compose Updates:**

- Updated to use development Dockerfiles by default
- Added volume mounts for source code hot reloading
- Added environment variables for Spring Boot DevTools
- Exposed additional ports for LiveReload and HMR
- Configured proper dependency management for development workflow

**Benefits:**

- Instant code updates without container restarts
- Faster development cycle with automatic reloading
- Better developer experience with live feedback
- Maintains separation between development and production builds
- Enables IDE debugging capabilities

**Usage:**

```bash
# Start with hot reloading enabled
docker compose up --build

# Source code changes now reload automatically
# No need to restart containers
```

**Files Updated:**

- `docker-compose.yml` - Development configuration with hot reloading
- `Dockerfile.dev` - Backend development Dockerfile
- `frontend/Dockerfile.dev` - Frontend development Dockerfile

---

## **Next Planned Step**

Refactor backend import schema for automotive data (vehicle, dealer, warranty, fleet, service records). Add Redis-backed summary/query behavior and expose endpoints for listing imported records and statistics.

---

### **01/05/2026 - Technical Debt Cleanup Completed**

Comprehensive codebase review and technical debt removal across all components.

**Backend Java Code:**

- Removed redundant canonical constructor in `ImportError.java` record (Java auto-generates it)
- Removed redundant null checks for `@PathVariable Long id` parameters in `RecordsController` (Spring MVC handles this automatically)
- Added `@NonNull` annotations to ID parameters for proper null type safety
- Removed unused imports: `org.springframework.lang.NonNull`, `java.util.concurrent.TimeUnit` in `StatisticsService`
- Removed redundant `@SuppressWarnings("unused")` annotation from `StatisticsService` class
- Removed commented out import in `RedisConfig.java`
- Removed commented out import in `RecordsController.java`
- Replaced `System.err.println` with TODO comment for proper logging framework in `AutomotivePipelineService`

**Frontend Code:**

- No technical debt found - Vue.js code is clean and well-structured

**Configuration Files:**

- Removed trailing whitespace from: `style.css`, `Dockerfile`, `frontend/Dockerfile`, `.dockerignore`, `frontend/index.html`, `frontend/package.json`

**Benefits:**

- Cleaner, more maintainable code
- Eliminated redundant code patterns
- Improved type safety with proper annotations
- Better code hygiene with removed unused imports and commented code
- Consistent file formatting without trailing whitespace

**Verification:**

- All changes maintain backward compatibility
- No functional changes to application behavior
- Code compiles successfully with reduced warnings
