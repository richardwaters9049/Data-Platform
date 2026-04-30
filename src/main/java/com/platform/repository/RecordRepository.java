package com.platform.repository;

import com.platform.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data creates the CRUD implementation at runtime.
public interface RecordRepository extends JpaRepository<Record, Long> {
}
