package com.dataplatform.repository;

import com.dataplatform.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    List<Service> findByVehicleId(Long vehicleId);
    
    List<Service> findByDealerId(Long dealerId);
    
    List<Service> findByStatus(String status);
    
    List<Service> findByServiceDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(s) FROM Service s WHERE s.status = :status")
    Long countByStatus(String status);
    
    @Query("SELECT s.serviceType, COUNT(s) FROM Service s GROUP BY s.serviceType")
    List<Object[]> countByServiceType();
}
