package com.dataplatform.repository;

import com.dataplatform.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data creates the CRUD implementation at runtime.
public interface RecordRepository extends JpaRepository<Record, Long> {
}
