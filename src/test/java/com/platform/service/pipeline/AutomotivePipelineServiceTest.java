package com.platform.service.pipeline;

import com.platform.dto.ImportResult;
import com.platform.model.DataType;
import com.platform.model.Dealer;
import com.platform.model.ServiceRecord;
import com.platform.model.Vehicle;
import com.platform.model.Warranty;
import com.platform.repository.DealerRepository;
import com.platform.repository.FleetRepository;
import com.platform.repository.ServiceRecordRepository;
import com.platform.repository.VehicleRepository;
import com.platform.repository.WarrantyRepository;
import com.platform.service.ingestion.CsvIngestionService;
import com.platform.service.transformation.AutomotiveDataTransformer;
import com.platform.service.validation.AutomotiveDataValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression tests for the automotive ETL pipeline relationship handling.
 */
class AutomotivePipelineServiceTest {

    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final DealerRepository dealerRepository = mock(DealerRepository.class);
    private final WarrantyRepository warrantyRepository = mock(WarrantyRepository.class);
    private final FleetRepository fleetRepository = mock(FleetRepository.class);
    private final ServiceRecordRepository serviceRepository = mock(ServiceRecordRepository.class);
    private final CacheManager cacheManager = mock(CacheManager.class);

    private final AutomotivePipelineService service = new AutomotivePipelineService(
            new CsvIngestionService(),
            new AutomotiveDataValidator(),
            new AutomotiveDataTransformer(),
            vehicleRepository,
            dealerRepository,
            warrantyRepository,
            fleetRepository,
            serviceRepository,
            cacheManager
    );

    @Test
    void importsVehicleWithExistingDealerRelationship() throws Exception {
        Dealer dealer = dealer("DLR001");
        when(dealerRepository.findByCode("DLR001")).thenReturn(dealer);

        ImportResult result = service.runPipeline(file("""
                vin,make,model,year,trim,color,fuelType,transmission,engineSize,bodyStyle,dealerCode,status
                SALAB2BN1HH123456,Jaguar,F-Pace,2024,R-Dynamic,Blue,Petrol,Automatic,2.0,SUV,DLR001,Active
                """), DataType.VEHICLE);

        ArgumentCaptor<Vehicle> savedVehicle = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(savedVehicle.capture());

        assertSuccessfulImport(result);
        assertThat(savedVehicle.getValue().getDealer()).isSameAs(dealer);
    }

    @Test
    void clearsMissingDealerRelationshipBeforeSavingVehicle() throws Exception {
        when(dealerRepository.findByCode("UNKNOWN")).thenReturn(null);

        ImportResult result = service.runPipeline(file("""
                vin,make,model,year,trim,color,fuelType,transmission,engineSize,bodyStyle,dealerCode,status
                SALAB2BN1HH123457,Jaguar,F-Type,2023,R,Black,Petrol,Automatic,5.0,Coupe,UNKNOWN,Active
                """), DataType.VEHICLE);

        ArgumentCaptor<Vehicle> savedVehicle = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(savedVehicle.capture());

        assertSuccessfulImport(result);
        assertThat(savedVehicle.getValue().getDealer()).isNull();
    }

    @Test
    void importsWarrantyWithExistingVehicleRelationship() throws Exception {
        Vehicle vehicle = vehicle("SALAB2BN1HH123456");
        when(vehicleRepository.findByVin("SALAB2BN1HH123456")).thenReturn(vehicle);

        ImportResult result = service.runPipeline(file("""
                warrantyNumber,warrantyType,startDate,endDate,mileageLimit,coverage,deductible,vin,provider,status
                WRN-1001,Extended,2026-01-01,2029-01-01,60000,Powertrain,250,SALAB2BN1HH123456,Manufacturer,Active
                """), DataType.WARRANTY);

        ArgumentCaptor<Warranty> savedWarranty = ArgumentCaptor.forClass(Warranty.class);
        verify(warrantyRepository).save(savedWarranty.capture());

        assertSuccessfulImport(result);
        assertThat(savedWarranty.getValue().getVehicle()).isSameAs(vehicle);
    }

    @Test
    void clearsMissingVehicleRelationshipBeforeSavingWarranty() throws Exception {
        when(vehicleRepository.findByVin("MISSINGVIN1234567")).thenReturn(null);

        ImportResult result = service.runPipeline(file("""
                warrantyNumber,warrantyType,startDate,endDate,mileageLimit,coverage,deductible,vin,provider,status
                WRN-1002,Extended,2026-01-01,2029-01-01,60000,Powertrain,250,MISSINGVIN1234567,Manufacturer,Active
                """), DataType.WARRANTY);

        ArgumentCaptor<Warranty> savedWarranty = ArgumentCaptor.forClass(Warranty.class);
        verify(warrantyRepository).save(savedWarranty.capture());

        assertSuccessfulImport(result);
        assertThat(savedWarranty.getValue().getVehicle()).isNull();
    }

    @Test
    void importsServiceRecordWithExistingVehicleAndDealerRelationships() throws Exception {
        Vehicle vehicle = vehicle("SALAB2BN1HH123456");
        Dealer dealer = dealer("DLR001");
        when(vehicleRepository.findByVin("SALAB2BN1HH123456")).thenReturn(vehicle);
        when(dealerRepository.findByCode("DLR001")).thenReturn(dealer);

        ImportResult result = service.runPipeline(file("""
                serviceNumber,serviceType,serviceDate,mileage,description,cost,vin,dealerCode,technician,status
                SRV-1001,Oil Change,2026-02-14,12000,Scheduled service,189.99,SALAB2BN1HH123456,DLR001,A Morgan,Completed
                """), DataType.SERVICE_RECORD);

        ArgumentCaptor<ServiceRecord> savedServiceRecord = ArgumentCaptor.forClass(ServiceRecord.class);
        verify(serviceRepository).save(savedServiceRecord.capture());

        assertSuccessfulImport(result);
        assertThat(savedServiceRecord.getValue().getVehicle()).isSameAs(vehicle);
        assertThat(savedServiceRecord.getValue().getDealer()).isSameAs(dealer);
    }

    @Test
    void clearsMissingRelationshipsBeforeSavingServiceRecord() throws Exception {
        when(vehicleRepository.findByVin("MISSINGVIN1234567")).thenReturn(null);
        when(dealerRepository.findByCode("UNKNOWN")).thenReturn(null);

        ImportResult result = service.runPipeline(file("""
                serviceNumber,serviceType,serviceDate,mileage,description,cost,vin,dealerCode,technician,status
                SRV-1002,Brake Service,2026-03-21,18000,Brake inspection,349.99,MISSINGVIN1234567,UNKNOWN,J Patel,Completed
                """), DataType.SERVICE_RECORD);

        ArgumentCaptor<ServiceRecord> savedServiceRecord = ArgumentCaptor.forClass(ServiceRecord.class);
        verify(serviceRepository).save(savedServiceRecord.capture());

        assertSuccessfulImport(result);
        assertThat(savedServiceRecord.getValue().getVehicle()).isNull();
        assertThat(savedServiceRecord.getValue().getDealer()).isNull();
    }

    private void assertSuccessfulImport(ImportResult result) {
        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsImported()).isEqualTo(1);
        assertThat(result.rowsRejected()).isZero();
        assertThat(result.errors()).isEmpty();
    }

    private MockMultipartFile file(String content) {
        return new MockMultipartFile(
                "file",
                "automotive.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Dealer dealer(String code) {
        Dealer dealer = new Dealer();
        dealer.setCode(code);
        return dealer;
    }

    private Vehicle vehicle(String vin) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(vin);
        return vehicle;
    }
}
