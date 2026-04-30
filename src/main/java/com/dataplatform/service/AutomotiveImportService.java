package com.dataplatform.service;

import com.dataplatform.dto.ImportError;
import com.dataplatform.dto.ImportResult;
import com.dataplatform.model.*;
import com.dataplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AutomotiveImportService {

    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;
    private final WarrantyRepository warrantyRepository;
    private final FleetRepository fleetRepository;
    private final ServiceRecordRepository serviceRepository;

    public AutomotiveImportService(
            VehicleRepository vehicleRepository,
            DealerRepository dealerRepository,
            WarrantyRepository warrantyRepository,
            FleetRepository fleetRepository,
            ServiceRecordRepository serviceRepository) {
        this.vehicleRepository = vehicleRepository;
        this.dealerRepository = dealerRepository;
        this.warrantyRepository = warrantyRepository;
        this.fleetRepository = fleetRepository;
        this.serviceRepository = serviceRepository;
    }

    public ImportResult importAutomotiveData(MultipartFile file, DataType dataType) throws IOException {
        List<ImportError> errors = new ArrayList<>();
        int rowsRead = 0;
        int rowsImported = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNumber = 0;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (rowNumber == 1) {
                    continue; // Skip header
                }

                if (line.isBlank()) {
                    continue;
                }

                rowsRead++;
                ImportResult result = processRow(line, dataType, rowNumber);
                
                errors.addAll(result.errors());
                rowsImported += result.rowsImported();
            }
        }

        return new ImportResult(rowsRead, rowsImported, rowsRead - rowsImported, List.copyOf(errors));
    }

    private ImportResult processRow(String line, DataType dataType, int rowNumber) {
        String[] parts = line.split(",", -1);
        String[] expectedFields = dataType.getSchemaFields();
        List<ImportError> errors = new ArrayList<>();

        if (parts.length != expectedFields.length) {
            errors.add(new ImportError(rowNumber, "row", 
                String.format("Expected %d columns: %s", expectedFields.length, String.join(",", expectedFields))));
            return new ImportResult(1, 0, 1, errors);
        }

        try {
            switch (dataType) {
                case VEHICLE:
                    return processVehicle(parts, rowNumber);
                case DEALER:
                    return processDealer(parts, rowNumber);
                case WARRANTY:
                    return processWarranty(parts, rowNumber);
                case FLEET:
                    return processFleet(parts, rowNumber);
                case SERVICE_RECORD:
                    return processServiceRecord(parts, rowNumber);
                default:
                    errors.add(new ImportError(rowNumber, "row", "Unsupported data type: " + dataType));
                    return new ImportResult(1, 0, 1, errors);
            }
        } catch (Exception e) {
            errors.add(new ImportError(rowNumber, "row", "Processing error: " + e.getMessage()));
            return new ImportResult(1, 0, 1, errors);
        }
    }

    private ImportResult processVehicle(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String vin = parts[0].trim();
        String make = parts[1].trim();
        String model = parts[2].trim();
        String yearText = parts[3].trim();
        String trim = parts[4].trim();
        String color = parts[5].trim();
        String fuelType = parts[6].trim();
        String transmission = parts[7].trim();
        String engineSizeText = parts[8].trim();
        String bodyStyle = parts[9].trim();
        String dealerCode = parts[10].trim();
        String status = parts[11].trim();

        // Validation
        if (vin.isBlank()) errors.add(new ImportError(rowNumber, "vin", "VIN is required"));
        if (make.isBlank()) errors.add(new ImportError(rowNumber, "make", "Make is required"));
        if (model.isBlank()) errors.add(new ImportError(rowNumber, "model", "Model is required"));
        
        Integer year = null;
        if (yearText.isBlank()) {
            errors.add(new ImportError(rowNumber, "year", "Year is required"));
        } else {
            try {
                year = Integer.parseInt(yearText);
                if (year < 1900 || year > 2100) {
                    errors.add(new ImportError(rowNumber, "year", "Year must be between 1900 and 2100"));
                }
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "year", "Year must be a valid number"));
            }
        }

        Double engineSize = null;
        if (!engineSizeText.isBlank()) {
            try {
                engineSize = Double.parseDouble(engineSizeText);
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "engineSize", "Engine size must be a valid number"));
            }
        }

        if (!errors.isEmpty()) {
            return new ImportResult(1, 0, 1, errors);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setVin(vin);
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setTrim(trim);
        vehicle.setColor(color);
        vehicle.setFuelType(fuelType);
        vehicle.setTransmission(transmission);
        vehicle.setEngineSize(engineSize);
        vehicle.setBodyStyle(bodyStyle);
        vehicle.setStatus(status);

        // Find dealer by code if provided
        if (!dealerCode.isBlank()) {
            Dealer dealer = dealerRepository.findByCode(dealerCode);
            if (dealer != null) {
                vehicle.setDealer(dealer);
            }
        }

        vehicleRepository.save(vehicle);
        return new ImportResult(1, 1, 0, List.of());
    }

    private ImportResult processDealer(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String code = parts[0].trim();
        String name = parts[1].trim();
        String address = parts[2].trim();
        String city = parts[3].trim();
        String state = parts[4].trim();
        String zipCode = parts[5].trim();
        String phone = parts[6].trim();
        String email = parts[7].trim();
        String website = parts[8].trim();
        String status = parts[9].trim();

        // Validation
        if (code.isBlank()) errors.add(new ImportError(rowNumber, "code", "Dealer code is required"));
        if (name.isBlank()) errors.add(new ImportError(rowNumber, "name", "Dealer name is required"));

        if (!errors.isEmpty()) {
            return new ImportResult(1, 0, 1, errors);
        }

        Dealer dealer = new Dealer();
        dealer.setCode(code);
        dealer.setName(name);
        dealer.setAddress(address);
        dealer.setCity(city);
        dealer.setState(state);
        dealer.setZipCode(zipCode);
        dealer.setPhone(phone);
        dealer.setEmail(email);
        dealer.setWebsite(website);
        dealer.setStatus(status);

        dealerRepository.save(dealer);
        return new ImportResult(1, 1, 0, List.of());
    }

    private ImportResult processWarranty(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String warrantyNumber = parts[0].trim();
        String warrantyType = parts[1].trim();
        String startDateText = parts[2].trim();
        String endDateText = parts[3].trim();
        String mileageLimitText = parts[4].trim();
        String coverage = parts[5].trim();
        String deductibleText = parts[6].trim();
        String vin = parts[7].trim();
        String provider = parts[8].trim();
        String status = parts[9].trim();

        // Validation
        if (warrantyNumber.isBlank()) errors.add(new ImportError(rowNumber, "warrantyNumber", "Warranty number is required"));

        LocalDate startDate = null;
        if (!startDateText.isBlank()) {
            try {
                startDate = LocalDate.parse(startDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add(new ImportError(rowNumber, "startDate", "Start date must be in yyyy-MM-dd format"));
            }
        }

        LocalDate endDate = null;
        if (!endDateText.isBlank()) {
            try {
                endDate = LocalDate.parse(endDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add(new ImportError(rowNumber, "endDate", "End date must be in yyyy-MM-dd format"));
            }
        }

        Integer mileageLimit = null;
        if (!mileageLimitText.isBlank()) {
            try {
                mileageLimit = Integer.parseInt(mileageLimitText);
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "mileageLimit", "Mileage limit must be a valid number"));
            }
        }

        Double deductible = null;
        if (!deductibleText.isBlank()) {
            try {
                deductible = Double.parseDouble(deductibleText);
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "deductible", "Deductible must be a valid number"));
            }
        }

        if (!errors.isEmpty()) {
            return new ImportResult(1, 0, 1, errors);
        }

        Warranty warranty = new Warranty();
        warranty.setWarrantyNumber(warrantyNumber);
        warranty.setWarrantyType(warrantyType);
        warranty.setStartDate(startDate);
        warranty.setEndDate(endDate);
        warranty.setMileageLimit(mileageLimit);
        warranty.setCoverage(coverage);
        warranty.setDeductible(deductible);
        warranty.setProvider(provider);
        warranty.setStatus(status);

        // Find vehicle by VIN if provided
        if (!vin.isBlank()) {
            Vehicle vehicle = vehicleRepository.findByVin(vin);
            if (vehicle != null) {
                warranty.setVehicle(vehicle);
            }
        }

        warrantyRepository.save(warranty);
        return new ImportResult(1, 1, 0, List.of());
    }

    private ImportResult processFleet(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String fleetCode = parts[0].trim();
        String fleetName = parts[1].trim();
        String company = parts[2].trim();
        String address = parts[3].trim();
        String city = parts[4].trim();
        String state = parts[5].trim();
        String zipCode = parts[6].trim();
        String contactPerson = parts[7].trim();
        String contactPhone = parts[8].trim();
        String contactEmail = parts[9].trim();
        String vehicleCountText = parts[10].trim();
        String status = parts[11].trim();

        // Validation
        if (fleetCode.isBlank()) errors.add(new ImportError(rowNumber, "fleetCode", "Fleet code is required"));
        if (fleetName.isBlank()) errors.add(new ImportError(rowNumber, "fleetName", "Fleet name is required"));

        Integer vehicleCount = null;
        if (!vehicleCountText.isBlank()) {
            try {
                vehicleCount = Integer.parseInt(vehicleCountText);
                if (vehicleCount < 0) {
                    errors.add(new ImportError(rowNumber, "vehicleCount", "Vehicle count must be positive"));
                }
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "vehicleCount", "Vehicle count must be a valid number"));
            }
        }

        if (!errors.isEmpty()) {
            return new ImportResult(1, 0, 1, errors);
        }

        Fleet fleet = new Fleet();
        fleet.setFleetCode(fleetCode);
        fleet.setFleetName(fleetName);
        fleet.setCompany(company);
        fleet.setAddress(address);
        fleet.setCity(city);
        fleet.setState(state);
        fleet.setZipCode(zipCode);
        fleet.setContactPerson(contactPerson);
        fleet.setContactPhone(contactPhone);
        fleet.setContactEmail(contactEmail);
        fleet.setVehicleCount(vehicleCount);
        fleet.setStatus(status);

        fleetRepository.save(fleet);
        return new ImportResult(1, 1, 0, List.of());
    }

    private ImportResult processServiceRecord(String[] parts, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        String serviceNumber = parts[0].trim();
        String serviceType = parts[1].trim();
        String serviceDateText = parts[2].trim();
        String mileageText = parts[3].trim();
        String description = parts[4].trim();
        String costText = parts[5].trim();
        String vin = parts[6].trim();
        String dealerCode = parts[7].trim();
        String technician = parts[8].trim();
        String status = parts[9].trim();

        // Validation
        if (serviceNumber.isBlank()) errors.add(new ImportError(rowNumber, "serviceNumber", "Service number is required"));

        LocalDate serviceDate = null;
        if (serviceDateText.isBlank()) {
            errors.add(new ImportError(rowNumber, "serviceDate", "Service date is required"));
        } else {
            try {
                serviceDate = LocalDate.parse(serviceDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add(new ImportError(rowNumber, "serviceDate", "Service date must be in yyyy-MM-dd format"));
            }
        }

        Integer mileage = null;
        if (!mileageText.isBlank()) {
            try {
                mileage = Integer.parseInt(mileageText);
                if (mileage < 0) {
                    errors.add(new ImportError(rowNumber, "mileage", "Mileage must be positive"));
                }
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "mileage", "Mileage must be a valid number"));
            }
        }

        Double cost = null;
        if (!costText.isBlank()) {
            try {
                cost = Double.parseDouble(costText);
                if (cost < 0) {
                    errors.add(new ImportError(rowNumber, "cost", "Cost must be positive"));
                }
            } catch (NumberFormatException e) {
                errors.add(new ImportError(rowNumber, "cost", "Cost must be a valid number"));
            }
        }

        if (!errors.isEmpty()) {
            return new ImportResult(1, 0, 1, errors);
        }

        ServiceRecord service = new ServiceRecord();
        service.setServiceNumber(serviceNumber);
        service.setServiceType(serviceType);
        service.setServiceDate(serviceDate);
        service.setMileage(mileage);
        service.setDescription(description);
        service.setCost(cost);
        service.setTechnician(technician);
        service.setStatus(status);

        // Find vehicle by VIN if provided
        if (!vin.isBlank()) {
            Vehicle vehicle = vehicleRepository.findByVin(vin);
            if (vehicle != null) {
                service.setVehicle(vehicle);
            }
        }

        // Find dealer by code if provided
        if (!dealerCode.isBlank()) {
            Dealer dealer = dealerRepository.findByCode(dealerCode);
            if (dealer != null) {
                service.setDealer(dealer);
            }
        }

        serviceRepository.save(service);
        return new ImportResult(1, 1, 0, List.of());
    }
}
