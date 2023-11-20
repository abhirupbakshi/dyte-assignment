package com.example.logging.web.controller;

import com.example.logging.exception.IllegalHttpRequestException;
import com.example.logging.service.JwtTokenService;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AdditionalAuthOperationController {

    private final JwtTokenService jwtTokenService;

    @Autowired
    public AdditionalAuthOperationController(JwtTokenService jwtTokenService) {

        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> postLogin(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalHttpRequestException("Basic authentication credentials are needed");
        }
        Assert.notNull(authentication.getName(), "Username cannot be null");

        String username = authentication.getName();
        String[] authorities;
        String jwt;

        authorities =
                authentication.getAuthorities().stream()
                        .map(
                                role -> {
                                    Assert.notNull(role, "GrantedAuthority is null for username: " + username);
                                    return role.getAuthority();
                                })
                        .toArray(String[]::new);

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(username)
                        .password("")
                        .roles(authorities)
                        .build();
        jwt = jwtTokenService.createJwtToken(userDetails);

        return ResponseEntity
                .accepted()
                .header("token", jwt)
                .header("Access-Control-Expose-Headers", "token")
                .build();
    }

    @GetMapping("verify/{token}")
    public ResponseEntity<Void> verifyJwtToken(@PathVariable("token") String token) {

        Assert.notNull(token, "JWT token cannot be null");

        try {
            jwtTokenService.extractUserDetails(token);
        } catch (JwtException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(null);
    }
}
