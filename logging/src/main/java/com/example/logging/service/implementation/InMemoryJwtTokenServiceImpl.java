package com.example.logging.service.implementation;

import com.example.logging.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryJwtTokenServiceImpl implements JwtTokenService {

    private final String userRoleSeparator = ":";
    private final String jwtUserRoleClaimFieldName = "roles";
    private final String jwtAccountExpiredFieldName = "isAccountExpired";
    private final String jwtAccountLockedFieldName = "isAccountLocked";
    private final String jwtCredentialsExpiredFieldName = "isCredentialsExpired";
    private final long jwtExpirationSeconds;
    private final Key jwtSigningKey;
    private final Set<String> blacklisted = new HashSet<>();

    @Autowired
    public InMemoryJwtTokenServiceImpl(@Qualifier("jwtExpirationSeconds") long jwtExpirationSeconds, Key jwtSigningKey) {

        this.jwtExpirationSeconds = jwtExpirationSeconds;
        this.jwtSigningKey = jwtSigningKey;
    }

    @Override
    public boolean isJwtTokenBlacklisted(String jwt) {

        return blacklisted.contains(jwt);
    }

    @Override
    public String createJwtToken(UserDetails userDetails) {

        Assert.notNull(userDetails, "User must not be null");

        String authorities = userDetails.getAuthorities() == null ?
                "" :
                userDetails
                        .getAuthorities()
                        .stream()
                        .map(role -> role.getAuthority().split("_")[1])
                        .collect(Collectors.joining(userRoleSeparator));

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim(jwtUserRoleClaimFieldName, authorities)
                .claim(jwtAccountExpiredFieldName, String.valueOf(!userDetails.isAccountNonExpired()))
                .claim(jwtAccountLockedFieldName, String.valueOf(!userDetails.isAccountNonLocked()))
                .claim(jwtCredentialsExpiredFieldName, String.valueOf(!userDetails.isCredentialsNonExpired()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationSeconds * 1000))
                .signWith(jwtSigningKey)
                .compact();
    }

    @Override
    public void blackListJwtToken(String jwt) {

        Assert.notNull(jwt, "JWT must not be null");

        blacklisted.add(jwt);
    }

    @Override
    public UserDetails extractUserDetails(String jwt) {

        Assert.notNull(jwt, "JWT must not be null");

        Jws<Claims> jws = Jwts
                .parserBuilder()
                .setSigningKey(jwtSigningKey)
                .build()
                .parseClaimsJws(jwt);

        List<String> roles = Arrays
                .stream(jws
                        .getBody()
                        .get(jwtUserRoleClaimFieldName, String.class)
                        .split(userRoleSeparator)
                )
                .toList();

        UserDetails user = User.builder()
                .password(jwt)
                .username(jws.getBody().getSubject())
                .roles(roles.toArray(new String[0]))
                .accountExpired(Boolean.parseBoolean(jws.getBody().get(jwtAccountExpiredFieldName, String.class)))
                .accountLocked(Boolean.parseBoolean(jws.getBody().get(jwtAccountLockedFieldName, String.class)))
                .credentialsExpired(Boolean.parseBoolean(jws.getBody().get(jwtCredentialsExpiredFieldName, String.class)))
                .build();

        if (isJwtTokenBlacklisted(jwt)) {
            throw new JwtException("Jwt token is invalid");
        }

        return user;
    }
}
