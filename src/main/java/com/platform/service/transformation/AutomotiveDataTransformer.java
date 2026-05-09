package com.platform.service.transformation;

import com.platform.model.*;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * Handles data transformation and normalization for automotive records.
 * Responsibility: Normalize and transform valid data only.
 * Assumes input data has already passed validation.
 */
@Component
public class AutomotiveDataTransformer {

    /**
     * Transforms raw CSV field values into a Vehicle entity.
     * Applies normalization and derived field calculations.
     * 
     * @param parts Raw CSV field values (already validated)
     * @return Transformed Vehicle entity
     */
    public Vehicle transformVehicle(String[] parts) {
        Vehicle vehicle = new Vehicle();
        
        // Basic field assignment with normalization
        vehicle.setVin(normalizeString(parts[0].trim()));
        vehicle.setMake(normalizeString(parts[1].trim()));
        vehicle.setModel(normalizeString(parts[2].trim()));
        vehicle.setYear(parseInteger(parts[3].trim()));
        vehicle.setTrim(normalizeString(parts[4].trim()));
        vehicle.setColor(normalizeString(parts[5].trim()));
        vehicle.setFuelType(normalizeEnum(parts[6].trim()));
        vehicle.setTransmission(normalizeEnum(parts[7].trim()));
        vehicle.setEngineSize(parseDouble(parts[8].trim()));
        vehicle.setBodyStyle(normalizeString(parts[9].trim()));
        vehicle.setStatus(normalizeStatus(parts[11].trim()));
        String dealerCode = normalizeString(parts[10].trim());
        if (dealerCode != null) {
            Dealer dealer = new Dealer();
            dealer.setCode(dealerCode.toUpperCase());
            vehicle.setDealer(dealer);
        }

        // Derived fields could be calculated here
        // Example: vehicle.setVehicleAge(calculateVehicleAge(vehicle.getYear()));

        return vehicle;
    }

    /**
     * Transforms raw CSV field values into a Dealer entity.
     */
    public Dealer transformDealer(String[] parts) {
        Dealer dealer = new Dealer();
        
        dealer.setCode(normalizeString(parts[0].trim()).toUpperCase());
        dealer.setName(normalizeString(parts[1].trim()));
        dealer.setAddress(normalizeString(parts[2].trim()));
        dealer.setCity(normalizeString(parts[3].trim()));
        dealer.setState(normalizeString(parts[4].trim()).toUpperCase());
        dealer.setZipCode(normalizeString(parts[5].trim()));
        dealer.setPhone(normalizePhone(parts[6].trim()));
        dealer.setEmail(normalizeEmail(parts[7].trim()));
        dealer.setWebsite(normalizeWebsite(parts[8].trim()));
        dealer.setStatus(normalizeStatus(parts[9].trim()));

        return dealer;
    }

    /**
     * Transforms raw CSV field values into a Warranty entity.
     */
    public Warranty transformWarranty(String[] parts) {
        Warranty warranty = new Warranty();
        
        warranty.setWarrantyNumber(normalizeString(parts[0].trim()));
        warranty.setWarrantyType(normalizeString(parts[1].trim()));
        warranty.setStartDate(parseDate(parts[2].trim()));
        warranty.setEndDate(parseDate(parts[3].trim()));
        warranty.setMileageLimit(parseInteger(parts[4].trim()));
        warranty.setCoverage(normalizeString(parts[5].trim()));
        warranty.setDeductible(parseDouble(parts[6].trim()));
        warranty.setProvider(normalizeString(parts[8].trim()));
        warranty.setStatus(normalizeStatus(parts[9].trim()));
        String vin = normalizeString(parts[7].trim());
        if (vin != null) {
            Vehicle vehicle = new Vehicle();
            vehicle.setVin(vin.toUpperCase());
            warranty.setVehicle(vehicle);
        }

        // Derived field: warranty duration could be calculated here
        // warranty.setDurationDays(calculateWarrantyDuration(warranty.getStartDate(), warranty.getEndDate()));

        return warranty;
    }

    /**
     * Transforms raw CSV field values into a Fleet entity.
     */
    public Fleet transformFleet(String[] parts) {
        Fleet fleet = new Fleet();
        
        fleet.setFleetCode(normalizeString(parts[0].trim()).toUpperCase());
        fleet.setFleetName(normalizeString(parts[1].trim()));
        fleet.setCompany(normalizeString(parts[2].trim()));
        fleet.setAddress(normalizeString(parts[3].trim()));
        fleet.setCity(normalizeString(parts[4].trim()));
        fleet.setState(normalizeString(parts[5].trim()).toUpperCase());
        fleet.setZipCode(normalizeString(parts[6].trim()));
        fleet.setContactPerson(normalizeString(parts[7].trim()));
        fleet.setContactPhone(normalizePhone(parts[8].trim()));
        fleet.setContactEmail(normalizeEmail(parts[9].trim()));
        fleet.setVehicleCount(parseInteger(parts[10].trim()));
        fleet.setStatus(normalizeStatus(parts[11].trim()));

        return fleet;
    }

    /**
     * Transforms raw CSV field values into a ServiceRecord entity.
     */
    public ServiceRecord transformServiceRecord(String[] parts) {
        ServiceRecord service = new ServiceRecord();
        
        service.setServiceNumber(normalizeString(parts[0].trim()));
        service.setServiceType(normalizeString(parts[1].trim()));
        service.setServiceDate(parseDate(parts[2].trim()));
        service.setMileage(parseInteger(parts[3].trim()));
        service.setDescription(normalizeString(parts[4].trim()));
        service.setCost(parseDouble(parts[5].trim()));
        String vin = normalizeString(parts[6].trim());
        if (vin != null) {
            Vehicle vehicle = new Vehicle();
            vehicle.setVin(vin.toUpperCase());
            service.setVehicle(vehicle);
        }
        String dealerCode = normalizeString(parts[7].trim());
        if (dealerCode != null) {
            Dealer dealer = new Dealer();
            dealer.setCode(dealerCode.toUpperCase());
            service.setDealer(dealer);
        }
        service.setTechnician(normalizeString(parts[8].trim()));
        service.setStatus(normalizeStatus(parts[9].trim()));

        return service;
    }

    // TRANSFORMATION HELPER METHODS

    private String normalizeString(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private String normalizeEnum(String value) {
        if (value == null || value.isBlank()) return null;
        // Convert to uppercase enum format
        return value.trim().toUpperCase().replace(" ", "_");
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) return null;
        // Normalize status values to consistent format
        String normalized = value.trim().toUpperCase();
        // Map common variations to standard values
        switch (normalized) {
            case "ACTIVE":
            case "ENABLED":
            case "ON":
                return "ACTIVE";
            case "INACTIVE":
            case "DISABLED":
            case "OFF":
                return "INACTIVE";
            case "SCHEDULED":
            case "PENDING":
                return "SCHEDULED";
            case "COMPLETED":
            case "DONE":
                return "COMPLETED";
            default:
                return normalized;
        }
    }

    private String normalizePhone(String value) {
        if (value == null || value.isBlank()) return null;
        // Remove common phone number formatting characters
        return value.trim().replaceAll("[\\s\\-\\(\\)]", "");
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toLowerCase();
    }

    private String normalizeWebsite(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        return normalized;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private java.time.LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return java.time.LocalDate.parse(value, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return null;
        }
    }

    // DERIVED FIELD CALCULATIONS (examples for future use)

    private Integer calculateVehicleAge(Integer year) {
        if (year == null) return null;
        return Year.now().getValue() - year;
    }

    private Long calculateWarrantyDuration(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate == null || endDate == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }
}
