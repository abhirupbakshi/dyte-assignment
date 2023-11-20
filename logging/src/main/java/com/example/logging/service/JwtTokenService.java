package com.example.logging.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtTokenService {

    String createJwtToken(UserDetails userDetails);

    UserDetails extractUserDetails(String jwt);

    void blackListJwtToken(String jwt);

    boolean isJwtTokenBlacklisted(String jwt);
}
