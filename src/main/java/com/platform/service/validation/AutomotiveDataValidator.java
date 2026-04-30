package com.platform.service.validation;

import com.platform.dto.ImportError;
import com.platform.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles validation of automotive data records.
 * Responsibility: Validate data against business rules and constraints.
 * Does NOT modify data - only validates and returns errors.
 */
@Component
public class AutomotiveDataValidator {

    /**
     * Validates vehicle data fields.
     * 
     * @param parts Raw CSV field values
     * @param rowNumber Row number for error reporting
     * @return List of validation errors (empty if valid)
     */
    public List<ImportError> validateVehicle(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String vin = parts[0].trim();
        String make = parts[1].trim();
        String model = parts[2].trim();
        String yearText = parts[3].trim();
        String engineSizeText = parts[8].trim();

        // Required field validation
        if (vin.isBlank()) errors.add(new ImportError(rowNumber, "vin", "VIN is required"));
        if (make.isBlank()) errors.add(new ImportError(rowNumber, "make", "Make is required"));
        if (model.isBlank()) errors.add(new ImportError(rowNumber, "model", "Model is required"));
        
        // Year validation
        if (yearText.isBlank()) {
            errors.add(new ImportError(rowNumber, "year", "Year is required"));
        } else {
            try {
                int year = Integer.parseInt(yearText);
                if (year < 1900 || year > 2100) {
                    errors.add(new ImportError(rowNumber, "year", "Year must be between 1900 and 2100"));
                }
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "year", "Year must be a valid number"));
            }
        }

        // Engine size validation (optional)
        if (!engineSizeText.isBlank()) {
            try {
                Double.parseDouble(engineSizeText);
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "engineSize", "Engine size must be a valid number"));
            }
        }

        return errors;
    }

    /**
     * Validates dealer data fields.
     */
    public List<ImportError> validateDealer(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String code = parts[0].trim();
        String name = parts[1].trim();

        if (code.isBlank()) errors.add(new ImportError(rowNumber, "code", "Dealer code is required"));
        if (name.isBlank()) errors.add(new ImportError(rowNumber, "name", "Dealer name is required"));

        return errors;
    }

    /**
     * Validates warranty data fields.
     */
    public List<ImportError> validateWarranty(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String warrantyNumber = parts[0].trim();
        String startDateText = parts[2].trim();
        String endDateText = parts[3].trim();
        String mileageLimitText = parts[4].trim();
        String deductibleText = parts[6].trim();

        if (warrantyNumber.isBlank()) errors.add(new ImportError(rowNumber, "warrantyNumber", "Warranty number is required"));

        // Date validation
        if (!startDateText.isBlank()) {
            try {
                LocalDate.parse(startDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add(new ImportError(rowNumber, "startDate", "Start date must be in yyyy-MM-dd format"));
            }
        }

        if (!endDateText.isBlank()) {
            try {
                LocalDate.parse(endDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add(new ImportError(rowNumber, "endDate", "End date must be in yyyy-MM-dd format"));
            }
        }

        // Numeric validation
        if (!mileageLimitText.isBlank()) {
            try {
                Integer.parseInt(mileageLimitText);
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "mileageLimit", "Mileage limit must be a valid number"));
            }
        }

        if (!deductibleText.isBlank()) {
            try {
                Double.parseDouble(deductibleText);
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "deductible", "Deductible must be a valid number"));
            }
        }

        return errors;
    }

    /**
     * Validates fleet data fields.
     */
    public List<ImportError> validateFleet(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String fleetCode = parts[0].trim();
        String fleetName = parts[1].trim();
        String vehicleCountText = parts[10].trim();

        if (fleetCode.isBlank()) errors.add(new ImportError(rowNumber, "fleetCode", "Fleet code is required"));
        if (fleetName.isBlank()) errors.add(new ImportError(rowNumber, "fleetName", "Fleet name is required"));

        if (!vehicleCountText.isBlank()) {
            try {
                int vehicleCount = Integer.parseInt(vehicleCountText);
                if (vehicleCount < 0) {
                    errors.add(new ImportError(rowNumber, "vehicleCount", "Vehicle count must be positive"));
                }
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "vehicleCount", "Vehicle count must be a valid number"));
            }
        }

        return errors;
    }

    /**
     * Validates service record data fields.
     */
    public List<ImportError> validateServiceRecord(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String serviceNumber = parts[0].trim();
        String serviceDateText = parts[2].trim();
        String mileageText = parts[3].trim();
        String costText = parts[5].trim();

        if (serviceNumber.isBlank()) errors.add(new ImportError(rowNumber, "serviceNumber", "Service number is required"));

        // Service date validation (required)
        if (serviceDateText.isBlank()) {
            errors.add(new ImportError(rowNumber, "serviceDate", "Service date is required"));
        } else {
            try {
                LocalDate.parse(serviceDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add(new ImportError(rowNumber, "serviceDate", "Service date must be in yyyy-MM-dd format"));
            }
        }

        // Numeric validation
        if (!mileageText.isBlank()) {
            try {
                int mileage = Integer.parseInt(mileageText);
                if (mileage < 0) {
                    errors.add(new ImportError(rowNumber, "mileage", "Mileage must be positive"));
                }
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "mileage", "Mileage must be a valid number"));
            }
        }

        if (!costText.isBlank()) {
            try {
                double cost = Double.parseDouble(costText);
                if (cost < 0) {
                    errors.add(new ImportError(rowNumber, "cost", "Cost must be positive"));
                }
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "cost", "Cost must be a valid number"));
            }
        }

        return errors;
    }
}
