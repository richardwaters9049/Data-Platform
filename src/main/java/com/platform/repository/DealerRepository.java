package com.platform.repository;

import com.platform.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    
    List<Dealer> findByStatus(String status);
    
    List<Dealer> findByCity(String city);
    
    List<Dealer> findByState(String state);
    
    Dealer findByCode(String code);
    
    @Query("SELECT COUNT(d) FROM Dealer d WHERE d.status = :status")
    Long countByStatus(String status);
    
    @Query("SELECT d.state, COUNT(d) FROM Dealer d GROUP BY d.state")
    List<Object[]> countByState();
}
