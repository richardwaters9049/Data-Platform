package com.dataplatform.service;

import com.dataplatform.repository.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class StatisticsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;
    private final WarrantyRepository warrantyRepository;
    private final FleetRepository fleetRepository;
    private final ServiceRecordRepository serviceRepository;

    private static final String STATS_KEY_PREFIX = "stats:";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(5);

    public StatisticsService(
            RedisTemplate<String, Object> redisTemplate,
            VehicleRepository vehicleRepository,
            DealerRepository dealerRepository,
            WarrantyRepository warrantyRepository,
            FleetRepository fleetRepository,
            ServiceRecordRepository serviceRepository) {
        this.redisTemplate = redisTemplate;
        this.vehicleRepository = vehicleRepository;
        this.dealerRepository = dealerRepository;
        this.warrantyRepository = warrantyRepository;
        this.fleetRepository = fleetRepository;
        this.serviceRepository = serviceRepository;
    }

    public Map<String, Object> getOverallStatistics() {
        String cacheKey = STATS_KEY_PREFIX + "overall";
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Map<String, Object> stats = new HashMap<>();
        
        // Vehicle statistics
        stats.put("totalVehicles", vehicleRepository.count());
        stats.put("vehiclesByStatus", getVehicleStatusCounts());
        stats.put("vehiclesByMake", getVehicleMakeCounts());
        
        // Dealer statistics
        stats.put("totalDealers", dealerRepository.count());
        stats.put("dealersByStatus", getDealerStatusCounts());
        stats.put("dealersByState", getDealerStateCounts());
        
        // Warranty statistics
        stats.put("totalWarranties", warrantyRepository.count());
        stats.put("warrantiesByStatus", getWarrantyStatusCounts());
        stats.put("warrantiesByType", getWarrantyTypeCounts());
        
        // Fleet statistics
        stats.put("totalFleets", fleetRepository.count());
        stats.put("fleetsByStatus", getFleetStatusCounts());
        stats.put("fleetsByState", getFleetStateCounts());
        
        // Service statistics
        stats.put("totalServices", serviceRepository.count());
        stats.put("servicesByStatus", getServiceStatusCounts());
        stats.put("servicesByType", getServiceTypeCounts());

        // Cache the results
        redisTemplate.opsForValue().set(cacheKey, stats, CACHE_DURATION);
        
        return stats;
    }

    public Map<String, Object> getVehicleStatistics() {
        String cacheKey = STATS_KEY_PREFIX + "vehicles";
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", vehicleRepository.count());
        stats.put("byStatus", getVehicleStatusCounts());
        stats.put("byMake", getVehicleMakeCounts());

        redisTemplate.opsForValue().set(cacheKey, stats, CACHE_DURATION);
        return stats;
    }

    public Map<String, Object> getDealerStatistics() {
        String cacheKey = STATS_KEY_PREFIX + "dealers";
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", dealerRepository.count());
        stats.put("byStatus", getDealerStatusCounts());
        stats.put("byState", getDealerStateCounts());

        redisTemplate.opsForValue().set(cacheKey, stats, CACHE_DURATION);
        return stats;
    }

    public void clearCache() {
        // Clear all statistics cache
        redisTemplate.delete(redisTemplate.keys(STATS_KEY_PREFIX + "*"));
    }

    private Map<String, Long> getVehicleStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("NEW", vehicleRepository.countByStatus("NEW"));
        counts.put("USED", vehicleRepository.countByStatus("USED"));
        counts.put("SOLD", vehicleRepository.countByStatus("SOLD"));
        return counts;
    }

    private Map<String, Long> getVehicleMakeCounts() {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> results = vehicleRepository.countByMake();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    private Map<String, Long> getDealerStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("ACTIVE", dealerRepository.countByStatus("ACTIVE"));
        counts.put("INACTIVE", dealerRepository.countByStatus("INACTIVE"));
        counts.put("SUSPENDED", dealerRepository.countByStatus("SUSPENDED"));
        return counts;
    }

    private Map<String, Long> getDealerStateCounts() {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> results = dealerRepository.countByState();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    private Map<String, Long> getWarrantyStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("ACTIVE", warrantyRepository.countByStatus("ACTIVE"));
        counts.put("EXPIRED", warrantyRepository.countByStatus("EXPIRED"));
        counts.put("CLAIMED", warrantyRepository.countByStatus("CLAIMED"));
        return counts;
    }

    private Map<String, Long> getWarrantyTypeCounts() {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> results = warrantyRepository.countByType();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    private Map<String, Long> getFleetStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("ACTIVE", fleetRepository.countByStatus("ACTIVE"));
        counts.put("INACTIVE", fleetRepository.countByStatus("INACTIVE"));
        counts.put("MAINTENANCE", fleetRepository.countByStatus("MAINTENANCE"));
        return counts;
    }

    private Map<String, Long> getFleetStateCounts() {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> results = fleetRepository.countByState();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    private Map<String, Long> getServiceStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("COMPLETED", serviceRepository.countByStatus("COMPLETED"));
        counts.put("SCHEDULED", serviceRepository.countByStatus("SCHEDULED"));
        counts.put("CANCELLED", serviceRepository.countByStatus("CANCELLED"));
        return counts;
    }

    private Map<String, Long> getServiceTypeCounts() {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> results = serviceRepository.countByServiceType();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }
}
