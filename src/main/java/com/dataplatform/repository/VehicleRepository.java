package com.dataplatform.repository;

import com.dataplatform.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    List<Vehicle> findByDealerId(Long dealerId);
    
    List<Vehicle> findByStatus(String status);
    
    Vehicle findByVin(String vin);
    
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = :status")
    Long countByStatus(String status);
    
    @Query("SELECT v.make, COUNT(v) FROM Vehicle v GROUP BY v.make")
    List<Object[]> countByMake();
}
