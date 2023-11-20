package com.example.logging.service;

import com.example.logging.model.Log;
import com.example.logging.model.Role;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface LogService {

    Log ingestLog(Log log);

    Page<Log> findLogs(String searchStr, Map<String, Object> filters, int page, int limit, List<Role> roles, Instant past, Instant future);
}
