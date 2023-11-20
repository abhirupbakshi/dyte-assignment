package com.example.logging.service.implementation;

import com.example.logging.model.Level;
import com.example.logging.model.Log;
import com.example.logging.model.Role;
import com.example.logging.repository.LogRepository;
import com.example.logging.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.example.logging.service.implementation.LogSpecification.*;

@Service
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;
    private final Map<Role, Level> logLevelPermissions;

    @Autowired
    public LogServiceImpl(LogRepository logRepository, @Qualifier("logLevelPermissions") Map<Role, Level> logLevelPermissions) {

        this.logRepository = logRepository;
        this.logLevelPermissions = logLevelPermissions;
    }

    private Pageable generatePageable(int page, int limit) {

        Assert.isTrue(page >= 1, "Page number cannot be less than 1");
        Assert.isTrue(limit >= 1, "Page limit cannot be less than 1");

        Sort sort = Sort.by("timestamp").descending();
        return PageRequest.of(page - 1, limit, sort);
    }

    @Transactional
    @Override
    public Log ingestLog(Log log) {

        log.setUuid(null);

        return logRepository.save(log);
    }

    @Override
    public Page<Log> findLogs(String searchStr, Map<String, Object> filters, int page, int limit, List<Role> roles, Instant past, Instant future) {

        Pageable pageable = generatePageable(page, limit);
        Specification<Log> spec =
                search(searchStr)
                        .and(filterByProperties(filters))
                        .and(filterByRoles(roles, logLevelPermissions))
                        .and(inTimestampRange(past, future));

        return logRepository.findAll(spec, pageable);
    }
}
