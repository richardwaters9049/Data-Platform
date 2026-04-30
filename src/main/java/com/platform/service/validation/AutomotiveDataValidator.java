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
     * Validates vehicle data fields with enhanced error reporting.
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
        
        // Reconstruct CSV line for error context
        String csvLine = String.join(",", parts);

        // Required field validation with column names
        if (vin.isBlank()) errors.add(ImportError.withColumn(rowNumber, "vin", "VIN is required", "VIN", csvLine));
        if (make.isBlank()) errors.add(ImportError.withColumn(rowNumber, "make", "Make is required", "Make", csvLine));
        if (model.isBlank()) errors.add(ImportError.withColumn(rowNumber, "model", "Model is required", "Model", csvLine));
        
        // Year validation with specific error messages
        if (yearText.isBlank()) {
            errors.add(ImportError.withColumn(rowNumber, "year", "Year is required", "Year", csvLine));
        } else {
            try {
                int year = Integer.parseInt(yearText);
                if (year < 1900 || year > 2100) {
                    errors.add(ImportError.withColumn(rowNumber, "year", 
                        String.format("Year '%s' must be between 1900 and 2100", yearText), "Year", csvLine));
                }
            } catch (NumberFormatException e) {
                errors.add(ImportError.withColumn(rowNumber, "year", 
                    String.format("Year '%s' is not a valid number", yearText), "Year", csvLine));
            }
        }

        // Engine size validation (optional) with specific error messages
        if (!engineSizeText.isBlank()) {
            try {
                Double.parseDouble(engineSizeText);
            } catch (NumberFormatException e) {
                errors.add(ImportError.withColumn(rowNumber, "engineSize", 
                    String.format("Engine size '%s' is not a valid number", engineSizeText), "Engine Size", csvLine));
            }
        }

        return errors;
    }

    /**
     * Validates dealer data fields with enhanced error reporting.
     */
    public List<ImportError> validateDealer(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String code = parts[0].trim();
        String name = parts[1].trim();
        
        // Reconstruct CSV line for error context
        String csvLine = String.join(",", parts);

        if (code.isBlank()) errors.add(ImportError.withColumn(rowNumber, "code", "Dealer code is required", "Dealer Code", csvLine));
        if (name.isBlank()) errors.add(ImportError.withColumn(rowNumber, "name", "Dealer name is required", "Dealer Name", csvLine));

        return errors;
    }

    /**
     * Validates warranty data fields with enhanced error reporting.
     */
    public List<ImportError> validateWarranty(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String warrantyNumber = parts[0].trim();
        String startDateText = parts[2].trim();
        String endDateText = parts[3].trim();
        String mileageLimitText = parts[4].trim();
        String deductibleText = parts[6].trim();
        
        // Reconstruct CSV line for error context
        String csvLine = String.join(",", parts);

        if (warrantyNumber.isBlank()) errors.add(ImportError.withColumn(rowNumber, "warrantyNumber", "Warranty number is required", "Warranty Number", csvLine));

        // Date validation with specific error messages
        if (!startDateText.isBlank()) {
            try {
                LocalDate.parse(startDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add(ImportError.withColumn(rowNumber, "startDate", 
                    String.format("Start date '%s' must be in yyyy-MM-dd format", startDateText), "Start Date", csvLine));
            }
        }

        if (!endDateText.isBlank()) {
            try {
                LocalDate.parse(endDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add(ImportError.withColumn(rowNumber, "endDate", 
                    String.format("End date '%s' must be in yyyy-MM-dd format", endDateText), "End Date", csvLine));
            }
        }

        // Numeric validation with specific error messages
        if (!mileageLimitText.isBlank()) {
            try {
                Integer.parseInt(mileageLimitText);
            } catch (NumberFormatException e) {
                errors.add(ImportError.withColumn(rowNumber, "mileageLimit", 
                    String.format("Mileage limit '%s' must be a valid number", mileageLimitText), "Mileage Limit", csvLine));
            }
        }

        if (!deductibleText.isBlank()) {
            try {
                Double.parseDouble(deductibleText);
            } catch (NumberFormatException e) {
                errors.add(ImportError.withColumn(rowNumber, "deductible", 
                    String.format("Deductible '%s' must be a valid number", deductibleText), "Deductible", csvLine));
            }
        }

        return errors;
    }

    /**
     * Validates fleet data fields with enhanced error reporting.
     */
    public List<ImportError> validateFleet(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String fleetCode = parts[0].trim();
        String fleetName = parts[1].trim();
        String vehicleCountText = parts[10].trim();
        
        // Reconstruct CSV line for error context
        String csvLine = String.join(",", parts);

        if (fleetCode.isBlank()) errors.add(ImportError.withColumn(rowNumber, "fleetCode", "Fleet code is required", "Fleet Code", csvLine));
        if (fleetName.isBlank()) errors.add(ImportError.withColumn(rowNumber, "fleetName", "Fleet name is required", "Fleet Name", csvLine));

        if (!vehicleCountText.isBlank()) {
            try {
                int vehicleCount = Integer.parseInt(vehicleCountText);
                if (vehicleCount < 0) {
                    errors.add(ImportError.withColumn(rowNumber, "vehicleCount", 
                        String.format("Vehicle count '%s' must be positive", vehicleCountText), "Vehicle Count", csvLine));
                }
            } catch (NumberFormatException e) {
                errors.add(ImportError.withColumn(rowNumber, "vehicleCount", 
                    String.format("Vehicle count '%s' must be a valid number", vehicleCountText), "Vehicle Count", csvLine));
            }
        }

        return errors;
    }

    /**
     * Validates service record data fields with enhanced error reporting.
     */
    public List<ImportError> validateServiceRecord(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String serviceNumber = parts[0].trim();
        String serviceDateText = parts[2].trim();
        String mileageText = parts[3].trim();
        String costText = parts[5].trim();
        
        // Reconstruct CSV line for error context
        String csvLine = String.join(",", parts);

        if (serviceNumber.isBlank()) errors.add(ImportError.withColumn(rowNumber, "serviceNumber", "Service number is required", "Service Number", csvLine));

        // Service date validation (required) with specific error messages
        if (serviceDateText.isBlank()) {
            errors.add(ImportError.withColumn(rowNumber, "serviceDate", "Service date is required", "Service Date", csvLine));
        } else {
            try {
                LocalDate.parse(serviceDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add(ImportError.withColumn(rowNumber, "serviceDate", 
                    String.format("Service date '%s' must be in yyyy-MM-dd format", serviceDateText), "Service Date", csvLine));
            }
        }

        // Numeric validation with specific error messages
        if (!mileageText.isBlank()) {
            try {
                int mileage = Integer.parseInt(mileageText);
                if (mileage < 0) {
                    errors.add(ImportError.withColumn(rowNumber, "mileage", 
                        String.format("Mileage '%s' must be positive", mileageText), "Mileage", csvLine));
                }
            } catch (NumberFormatException e) {
                errors.add(ImportError.withColumn(rowNumber, "mileage", 
                    String.format("Mileage '%s' must be a valid number", mileageText), "Mileage", csvLine));
            }
        }

        if (!costText.isBlank()) {
            try {
                double cost = Double.parseDouble(costText);
                if (cost < 0) {
                    errors.add(ImportError.withColumn(rowNumber, "cost", 
                        String.format("Cost '%s' must be positive", costText), "Cost", csvLine));
                }
            } catch (NumberFormatException e) {
                errors.add(ImportError.withColumn(rowNumber, "cost", 
                    String.format("Cost '%s' must be a valid number", costText), "Cost", csvLine));
            }
        }

        return errors;
    }
}
