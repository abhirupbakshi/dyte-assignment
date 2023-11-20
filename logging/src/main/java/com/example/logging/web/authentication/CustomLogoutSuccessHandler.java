package com.example.logging.web.authentication;

import com.example.logging.service.JwtTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtTokenService jwtTokenService;

    @Autowired
    public CustomLogoutSuccessHandler(JwtTokenService jwtTokenService) {

        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        Assert.notNull(request, "Http request cannot be null");

        Object attribute = request.getAttribute("token");

        if (attribute instanceof Map.Entry entry && entry.getKey() instanceof String username && entry.getValue() instanceof String token) {
            jwtTokenService.blackListJwtToken(token);
        }
    }
}
