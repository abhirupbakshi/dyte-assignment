package com.example.logging.web.controller;

import com.example.logging.exception.IllegalHttpRequestException;
import com.example.logging.exception.MappingUnsuccessfulException;
import com.example.logging.model.Level;
import com.example.logging.model.Log;
import com.example.logging.model.Role;
import com.example.logging.service.LogService;
import com.example.logging.utility.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class LogController {

    private final LogService logService;
    private final JsonUtils jsonUtils;
    private Instant lastAdded = null;

    @Autowired
    public LogController(LogService logService, JsonUtils jsonUtils) {

        this.logService = logService;
        this.jsonUtils = jsonUtils;
    }

    private Map<String, Object> validateProperties(Map<String, String> filters) {

        Map<String, Object> properties = new HashMap<>();

        for (Map.Entry<String, String> entry : filters.entrySet()) {

            if (entry.getKey().equals("page")
                    || entry.getKey().equals("limit")
                    || entry.getKey().equals("past")
                    || entry.getKey().equals("future")
                    || entry.getKey().equals("search")) {
                continue;
            }

            String name;
            Field field;
            Object value;

            try {
                Map.Entry<String, Field> e = jsonUtils.toClassField(entry.getKey(), Log.class);
                name = e.getKey();
                field = e.getValue();
            } catch (MappingUnsuccessfulException e) {
                throw new IllegalHttpRequestException("Invalid field: " + entry.getKey(), e);
            }

            if (field.getType().equals(UUID.class)) {
                value = UUID.fromString(entry.getValue());
            } else if (field.getType().equals(Level.class)) {
                value = Level.fromValue(entry.getValue());
            } else if (field.getType().equals(Instant.class)) {
                value = Instant.parse(entry.getValue());
            } else if (field.getType().equals(String.class)) {
                value = entry.getValue();
            } else {
                throw new RuntimeException("No field type mapping has been provided for field: " + field.getType());
            }

            properties.put(name, value);
        }

        return properties;
    }

    @PostMapping
    public ResponseEntity<Log> ingestLog(@RequestBody @Valid Log log, HttpServletRequest request) {

        ResponseEntity<Log> body = ResponseEntity.created(URI.create(request.getRequestURI())).body(logService.ingestLog(log));
        lastAdded = Instant.now();

        return body;
    }

    @GetMapping("last-ingested")
    public ResponseEntity<Map<String, Instant>> getLastAdded() {

        Map<String, Instant> m = new HashMap<>();
        m.put("timestamp", lastAdded);

        return ResponseEntity.ok(m);
    }

    @GetMapping
    public ResponseEntity<List<Log>> findLogs(
            @RequestParam(required = false) Map<String, String> filters,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(value = "search", required = false) String searchStr,
            @RequestParam(value = "past", required = false) Instant past,
            @RequestParam(value = "future", required = false) Instant future,
            Authentication authentication) {

        if (page < 1 || limit < 1) {
            throw new IllegalHttpRequestException("Page number or limit cannot be less than 1");
        }
        if (past == null ^ future == null) {
            throw new IllegalHttpRequestException("Both past and future parameter should be either empty or a valid timestamp value");
        }
        if (future != null && future.compareTo(past) < 0) {
            throw new IllegalHttpRequestException("Past timestamp cannot be after the future timestamp");
        }
        if (searchStr != null && searchStr.isBlank()) {
            throw new IllegalHttpRequestException("Search string cannot be blank if search parameter is used");
        }

        Map<String, Object> properties = validateProperties(filters);
        List<Role> roles = authentication.getAuthorities().stream().map(ga -> {
            String a = ga.getAuthority().contains("ROLE_") ? ga.getAuthority().split("_")[1] : ga.getAuthority();
            return new Role(a);
        }).toList();

        Page<Log> logPage = logService.findLogs(searchStr, properties, page, limit, roles, past, future);

        return ResponseEntity
                .ok()
                .header("X-Total-Count", String.valueOf(logPage.getTotalElements()))
                .header("Access-Control-Expose-Headers", "X-Total-Count")
                .body(logPage.getContent());
    }
}
