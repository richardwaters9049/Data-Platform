package com.platform.service.pipeline;

import com.platform.dto.ImportError;
import com.platform.dto.ImportResult;
import com.platform.model.*;
import com.platform.repository.*;
import com.platform.service.ingestion.CsvIngestionService;
import com.platform.service.transformation.AutomotiveDataTransformer;
import com.platform.service.validation.AutomotiveDataValidator;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Central orchestrator for the automotive ETL pipeline.
 * Coordinates the complete flow: ingestion -> validation -> transformation -> persistence.
 * 
 * Pipeline stages:
 * 1. INGESTION: CSV file reading and raw data extraction
 * 2. VALIDATION: Data quality checks and business rule validation
 * 3. TRANSFORMATION: Data normalization and derived field calculations
 * 4. PERSISTENCE: Database storage with transaction safety
 * 5. CACHE INVALIDATION: Trigger cache updates for statistics
 */
@Service
public class AutomotivePipelineService {

    private final CsvIngestionService ingestionService;
    private final AutomotiveDataValidator validator;
    private final AutomotiveDataTransformer transformer;
    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;
    private final WarrantyRepository warrantyRepository;
    private final FleetRepository fleetRepository;
    private final ServiceRecordRepository serviceRepository;
    private final CacheManager cacheManager;

    public AutomotivePipelineService(
            CsvIngestionService ingestionService,
            AutomotiveDataValidator validator,
            AutomotiveDataTransformer transformer,
            VehicleRepository vehicleRepository,
            DealerRepository dealerRepository,
            WarrantyRepository warrantyRepository,
            FleetRepository fleetRepository,
            ServiceRecordRepository serviceRepository,
            CacheManager cacheManager) {
        this.ingestionService = ingestionService;
        this.validator = validator;
        this.transformer = transformer;
        this.vehicleRepository = vehicleRepository;
        this.dealerRepository = dealerRepository;
        this.warrantyRepository = warrantyRepository;
        this.fleetRepository = fleetRepository;
        this.serviceRepository = serviceRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Executes the complete ETL pipeline for automotive data.
     * 
     * @param file The CSV file to process
     * @param dataType The type of automotive data being processed
     * @return ImportResult with processing statistics and any validation errors
     * @throws Exception If pipeline execution fails
     */
    public ImportResult runPipeline(MultipartFile file, DataType dataType) throws Exception {
        List<ImportError> allErrors = new ArrayList<>();
        int rowsRead = 0;
        int rowsImported = 0;

        // STAGE 1: INGESTION - Extract raw CSV data
        List<String> rawRows = ingestionService.ingestCsv(file, dataType);
        rowsRead = rawRows.size();

        // STAGE 2-4: Process each row through validation -> transformation -> persistence
        for (int i = 0; i < rawRows.size(); i++) {
            String rawRow = rawRows.get(i);
            int rowNumber = i + 2; // Account for header row

            PipelineResult result = processRowThroughPipeline(rawRow, dataType, rowNumber);
            
            allErrors.addAll(result.getErrors());
            if (result.isSuccess()) {
                rowsImported++;
            }
        }

        // STAGE 5: CACHE INVALIDATION - Clear statistics cache
        invalidateStatisticsCache();

        return new ImportResult(rowsRead, rowsImported, rowsRead - rowsImported, List.copyOf(allErrors));
    }

    /**
     * Processes a single row through the validation -> transformation -> persistence pipeline.
     */
    private PipelineResult processRowThroughPipeline(String rawRow, DataType dataType, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();

        // INGESTION: Parse CSV structure
        String[] parts = ingestionService.parseCsvStructure(rawRow, dataType);
        if (parts == null) {
            String[] expectedFields = dataType.getSchemaFields();
            String actualColumns = String.format("Found %d columns", rawRow.split(",").length);
            errors.add(ImportError.withColumn(rowNumber, "row", 
                String.format("CSV structure error: Expected %d columns (%s) but %s", 
                    expectedFields.length, String.join(",", expectedFields), actualColumns), 
                "CSV Structure", rawRow));
            return PipelineResult.failure(errors);
        }

        // VALIDATION: Validate data against business rules
        List<ImportError> validationErrors = validateRow(parts, dataType, rowNumber);
        if (!validationErrors.isEmpty()) {
            return PipelineResult.failure(validationErrors);
        }

        // TRANSFORMATION: Transform and normalize valid data
        Object transformedEntity = transformRow(parts, dataType);

        // PERSISTENCE: Save to database
        try {
            persistEntity(transformedEntity, dataType);
            return PipelineResult.success();
        } catch (Exception e) {
            errors.add(new ImportError(rowNumber, "row", "Persistence error: " + e.getMessage()));
            return PipelineResult.failure(errors);
        }
    }

    /**
     * Validates a row using the appropriate validator.
     */
    private List<ImportError> validateRow(String[] parts, DataType dataType, int rowNumber) {
        return switch (dataType) {
            case VEHICLE -> validator.validateVehicle(parts, rowNumber);
            case DEALER -> validator.validateDealer(parts, rowNumber);
            case WARRANTY -> validator.validateWarranty(parts, rowNumber);
            case FLEET -> validator.validateFleet(parts, rowNumber);
            case SERVICE_RECORD -> validator.validateServiceRecord(parts, rowNumber);
            default -> List.of(new ImportError(rowNumber, "row", "Unsupported data type: " + dataType));
        };
    }

    /**
     * Transforms a row using the appropriate transformer.
     */
    private Object transformRow(String[] parts, DataType dataType) {
        return switch (dataType) {
            case VEHICLE -> transformer.transformVehicle(parts);
            case DEALER -> transformer.transformDealer(parts);
            case WARRANTY -> transformer.transformWarranty(parts);
            case FLEET -> transformer.transformFleet(parts);
            case SERVICE_RECORD -> transformer.transformServiceRecord(parts);
            default -> throw new IllegalArgumentException("Unsupported data type: " + dataType);
        };
    }

    /**
     * Persists transformed entity to the appropriate repository.
     */
    private void persistEntity(Object entity, DataType dataType) {
        switch (dataType) {
            case VEHICLE -> {
                Vehicle vehicle = (Vehicle) entity;
                // Handle dealer relationship if dealer code is present
                if (vehicle.getDealer() != null && vehicle.getDealer().getCode() != null) {
                    Dealer dealer = dealerRepository.findByCode(vehicle.getDealer().getCode());
                    if (dealer != null) {
                        vehicle.setDealer(dealer);
                    }
                }
                vehicleRepository.save(vehicle);
            }
            case DEALER -> dealerRepository.save((Dealer) entity);
            case WARRANTY -> {
                Warranty warranty = (Warranty) entity;
                // Handle vehicle relationship if VIN is present
                if (warranty.getVehicle() != null && warranty.getVehicle().getVin() != null) {
                    Vehicle vehicle = vehicleRepository.findByVin(warranty.getVehicle().getVin());
                    if (vehicle != null) {
                        warranty.setVehicle(vehicle);
                    }
                }
                warrantyRepository.save(warranty);
            }
            case FLEET -> fleetRepository.save((Fleet) entity);
            case SERVICE_RECORD -> {
                ServiceRecord service = (ServiceRecord) entity;
                // Handle vehicle and dealer relationships
                if (service.getVehicle() != null && service.getVehicle().getVin() != null) {
                    Vehicle vehicle = vehicleRepository.findByVin(service.getVehicle().getVin());
                    if (vehicle != null) {
                        service.setVehicle(vehicle);
                    }
                }
                if (service.getDealer() != null && service.getDealer().getCode() != null) {
                    Dealer dealer = dealerRepository.findByCode(service.getDealer().getCode());
                    if (dealer != null) {
                        service.setDealer(dealer);
                    }
                }
                serviceRepository.save(service);
            }
            default -> throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }

    /**
     * Invalidates the statistics cache to ensure fresh data after import.
     */
    private void invalidateStatisticsCache() {
        try {
            var statisticsCache = cacheManager.getCache("statistics");
            if (statisticsCache != null) {
                statisticsCache.clear();
            }
        } catch (Exception e) {
            // Log cache invalidation error but don't fail the pipeline
            System.err.println("Cache invalidation failed: " + e.getMessage());
        }
    }

    /**
     * Internal result class for pipeline processing.
     */
    private static class PipelineResult {
        private final boolean success;
        private final List<ImportError> errors;

        private PipelineResult(boolean success, List<ImportError> errors) {
            this.success = success;
            this.errors = errors;
        }

        public boolean isSuccess() {
            return success;
        }

        public List<ImportError> getErrors() {
            return errors;
        }

        static PipelineResult success() {
            return new PipelineResult(true, List.of());
        }

        static PipelineResult failure(List<ImportError> errors) {
            return new PipelineResult(false, errors);
        }
    }
}
