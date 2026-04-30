package com.dataplatform.repository;

import com.dataplatform.model.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Long> {
    
    List<Warranty> findByVehicleId(Long vehicleId);
    
    List<Warranty> findByStatus(String status);
    
    List<Warranty> findByEndDateBefore(LocalDate date);
    
    @Query("SELECT COUNT(w) FROM Warranty w WHERE w.status = :status")
    Long countByStatus(String status);
    
    @Query("SELECT w.warrantyType, COUNT(w) FROM Warranty w GROUP BY w.warrantyType")
    List<Object[]> countByType();
}
