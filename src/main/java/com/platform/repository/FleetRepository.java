package com.platform.repository;

import com.platform.model.Fleet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FleetRepository extends JpaRepository<Fleet, Long> {
    
    List<Fleet> findByStatus(String status);
    
    List<Fleet> findByCity(String city);
    
    List<Fleet> findByState(String state);
    
    @Query("SELECT COUNT(f) FROM Fleet f WHERE f.status = :status")
    Long countByStatus(String status);
    
    @Query("SELECT f.state, COUNT(f) FROM Fleet f GROUP BY f.state")
    List<Object[]> countByState();
}
