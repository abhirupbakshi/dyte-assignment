package com.example.logging.configuration;

import com.example.logging.web.authentication.JwtFilter;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.Key;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    protected Key signingKey() {
        String key = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        return  Keys.hmacShaKeyFor(key.getBytes());
    }

    @Bean
    protected long jwtExpirationSeconds() {
        return 3600;
    }

    @Bean
    protected List<RequestMatcher> jwtFilterExclusions() {

        return List.of(
                new AntPathRequestMatcher( "/auth/verify/{token}", "GET"),
                new AntPathRequestMatcher( "/auth/login", "POST"),
                new AntPathRequestMatcher( "/last-ingested", "GET"),
                new AntPathRequestMatcher("/", "POST")
        );
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        configuration.addAllowedOrigin("http://127.0.0.1:5000");
        configuration.addAllowedOrigin("http://localhost:5000");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PATCH");
        configuration.addAllowedMethod("DELETE");
        configuration.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    protected HttpStatusEntryPoint unauthorizedEntryPoint() {

        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    @Bean
    protected GrantedAuthorityDefaults grantedAuthorityDefaults() {

        return new GrantedAuthorityDefaults("");
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter, LogoutSuccessHandler logoutSuccessHandler) throws Exception {

        return http
                .authorizeHttpRequests(req -> req
                        .requestMatchers(HttpMethod.POST, "/", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "last-ingested", "/auth/verify/{token}").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, LogoutFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(basic -> basic.authenticationEntryPoint(unauthorizedEntryPoint()))
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .logoutUrl("/auth/logout")
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}
