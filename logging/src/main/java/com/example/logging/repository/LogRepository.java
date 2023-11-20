package com.example.logging.repository;

import com.example.logging.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LogRepository extends JpaRepository<Log, UUID>, JpaSpecificationExecutor<Log> {
}
