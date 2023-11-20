package com.example.logging.web.authentication;

import com.example.logging.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final List<RequestMatcher> jwtFilterExclusions;
    private final HttpStatusEntryPoint unauthorizedEntryPoint;

    @Autowired
    public JwtFilter(
            JwtTokenService jwtTokenService,
            HttpStatusEntryPoint unauthorizedEntryPoint,
            @Qualifier("jwtFilterExclusions") List<RequestMatcher> jwtFilterExclusions) {

        this.jwtTokenService = jwtTokenService;
        this.unauthorizedEntryPoint = unauthorizedEntryPoint;
        this.jwtFilterExclusions = jwtFilterExclusions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            String bearerHeader = "Bearer";

            if (authHeader == null) {
                throw new IllegalArgumentException(HttpHeaders.AUTHORIZATION + " header was not found");
            } else if (!authHeader.startsWith(bearerHeader)) {
                throw new IllegalArgumentException("Invalid " + HttpHeaders.AUTHORIZATION + " header");
            }

            String jwt = authHeader.substring(bearerHeader.length() + 1);

            UserDetails user = jwtTokenService.extractUserDetails(jwt);
            Authentication authenticated = UsernamePasswordAuthenticationToken
                    .authenticated(user.getUsername(), jwt, user.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authenticated);
            SecurityContextHolder.setContext(context);
            request.setAttribute("token", Map.entry(user.getUsername(), jwt));
        } catch (RuntimeException e) {

            unauthorizedEntryPoint.commence(request, response, new AuthenticationException(e.getMessage(), e) {
            });

            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        return jwtFilterExclusions.stream().anyMatch(matcher -> matcher.matches(request));
    }

    // This logic is repeated in the logout filter also.
    public void blacklistJwtInRequestAttribute(HttpServletRequest request) {

        Assert.notNull(request, "Http request cannot be null");

        Object attribute = request.getAttribute("token");

        if (attribute instanceof Map.Entry entry && entry.getKey() instanceof String username && entry.getValue() instanceof String token) {
            jwtTokenService.blackListJwtToken(token);
        }
    }
}
