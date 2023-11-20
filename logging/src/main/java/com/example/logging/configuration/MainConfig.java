package com.example.logging.configuration;

import com.example.logging.model.Level;
import com.example.logging.model.Role;
import com.example.logging.repository.RoleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
public class MainConfig {

    @Bean
    protected Map<Role, Level> logLevelPermissions(RoleRepository roleRepository) {

        List<Role> roles = roleRepository.findAll();
        Map<Role, Level> map = new HashMap<>();

        Assert.isTrue(roles.size() != 0, "No user roles have been found on the database");

        for (Role role : roles) {

            if (role.getName().equals("ADMIN")) {
                map.put(role, Level.TRACE);
            } else if (role.getName().equals("DEVELOPER")) {
                map.put(role, Level.WARN);
            } else {
                throw new RuntimeException("No mapping found for setting log level permission for role: " + role.getName());
            }
        }

        return Collections.unmodifiableMap(map);
    }
}
