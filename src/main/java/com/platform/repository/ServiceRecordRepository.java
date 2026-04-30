package com.platform.repository;

import com.platform.model.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    
    List<ServiceRecord> findByVehicleId(Long vehicleId);
    
    List<ServiceRecord> findByDealerId(Long dealerId);
    
    List<ServiceRecord> findByStatus(String status);
    
    List<ServiceRecord> findByServiceDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(s) FROM ServiceRecord s WHERE s.status = :status")
    Long countByStatus(String status);
    
    @Query("SELECT s.serviceType, COUNT(s) FROM ServiceRecord s GROUP BY s.serviceType")
    List<Object[]> countByServiceType();
}
