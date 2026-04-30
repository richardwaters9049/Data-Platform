package com.dataplatform.controller;

import com.dataplatform.model.*;
import com.dataplatform.repository.*;
import com.dataplatform.service.StatisticsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordsController {

    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;
    private final WarrantyRepository warrantyRepository;
    private final FleetRepository fleetRepository;
    private final ServiceRecordRepository serviceRepository;
    private final StatisticsService statisticsService;

    public RecordsController(
            VehicleRepository vehicleRepository,
            DealerRepository dealerRepository,
            WarrantyRepository warrantyRepository,
            FleetRepository fleetRepository,
            ServiceRecordRepository serviceRepository,
            StatisticsService statisticsService) {
        this.vehicleRepository = vehicleRepository;
        this.dealerRepository = dealerRepository;
        this.warrantyRepository = warrantyRepository;
        this.fleetRepository = fleetRepository;
        this.serviceRepository = serviceRepository;
        this.statisticsService = statisticsService;
    }

    // Vehicle endpoints
    @GetMapping("/vehicles")
    public ResponseEntity<Page<Vehicle>> getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(vehicleRepository.findAll(pageable));
    }

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable Long id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vehicles/by-vin/{vin}")
    public ResponseEntity<Vehicle> getVehicleByVin(@PathVariable String vin) {
        Vehicle vehicle = vehicleRepository.findByVin(vin);
        return vehicle != null ? ResponseEntity.ok(vehicle) : ResponseEntity.notFound().build();
    }

    // Dealer endpoints
    @GetMapping("/dealers")
    public ResponseEntity<Page<Dealer>> getDealers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(dealerRepository.findAll(pageable));
    }

    @GetMapping("/dealers/{id}")
    public ResponseEntity<Dealer> getDealer(@PathVariable Long id) {
        return dealerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dealers/by-code/{code}")
    public ResponseEntity<Dealer> getDealerByCode(@PathVariable String code) {
        Dealer dealer = dealerRepository.findByCode(code);
        return dealer != null ? ResponseEntity.ok(dealer) : ResponseEntity.notFound().build();
    }

    // Warranty endpoints
    @GetMapping("/warranties")
    public ResponseEntity<Page<Warranty>> getWarranties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(warrantyRepository.findAll(pageable));
    }

    @GetMapping("/warranties/{id}")
    public ResponseEntity<Warranty> getWarranty(@PathVariable Long id) {
        return warrantyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Fleet endpoints
    @GetMapping("/fleets")
    public ResponseEntity<Page<Fleet>> getFleets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(fleetRepository.findAll(pageable));
    }

    @GetMapping("/fleets/{id}")
    public ResponseEntity<Fleet> getFleet(@PathVariable Long id) {
        return fleetRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Service endpoints
    @GetMapping("/services")
    public ResponseEntity<Page<ServiceRecord>> getServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(serviceRepository.findAll(pageable));
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceRecord> getService(@PathVariable Long id) {
        return serviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Statistics endpoints
    @GetMapping("/statistics/overall")
    public ResponseEntity<Map<String, Object>> getOverallStatistics() {
        return ResponseEntity.ok(statisticsService.getOverallStatistics());
    }

    @GetMapping("/statistics/vehicles")
    public ResponseEntity<Map<String, Object>> getVehicleStatistics() {
        return ResponseEntity.ok(statisticsService.getVehicleStatistics());
    }

    @GetMapping("/statistics/dealers")
    public ResponseEntity<Map<String, Object>> getDealerStatistics() {
        return ResponseEntity.ok(statisticsService.getDealerStatistics());
    }

    @PostMapping("/statistics/clear-cache")
    public ResponseEntity<Void> clearStatisticsCache() {
        statisticsService.clearCache();
        return ResponseEntity.ok().build();
    }
}
