package com.barbergarage.config;

import com.barbergarage.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource cors(
            @Value("${app.cors.allowed-origins}") String origins) {

        CorsConfiguration config = new CorsConfiguration();

        List<String> allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();

        config.setAllowedOrigins(allowedOrigins);

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"));

        config.setAllowedHeaders(List.of("*"));

        config.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type"));

        config.setAllowCredentials(true);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    SecurityFilterChain filter(
            HttpSecurity http,
            JwtAuthFilter jwt) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {
                })

                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // IMPORTANTE PARA CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // LOGIN
                        .requestMatchers(
                                "/api/auth/login")
                        .permitAll()

                        // RUTAS PÚBLICAS
                        .requestMatchers(
                                "/api/public/**")
                        .permitAll()

                        // SERVICIO IA
                        .requestMatchers(
                                "/api/ai/**")
                        .permitAll()

                        // ACTUATOR
                        .requestMatchers(
                                "/actuator/**")
                        .permitAll()

                        // ADMIN
                        .requestMatchers(
                                "/api/admin/**")
                        .hasRole("ADMIN")

                        // RESTO
                        .anyRequest()
                        .authenticated())

                .addFilterBefore(
                        jwt,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}