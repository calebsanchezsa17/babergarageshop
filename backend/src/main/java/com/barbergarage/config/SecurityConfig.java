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
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource(
                        @Value("${app.cors.allowed-origins}") String origins) {

                CorsConfiguration config = new CorsConfiguration();

                List<String> allowedOrigins = Arrays.stream(origins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isBlank())
                                .toList();

                // Importante para Railway y credenciales
                config.setAllowedOriginPatterns(allowedOrigins);

                config.setAllowedMethods(List.of(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",
                                "DELETE",
                                "OPTIONS"));

                config.setAllowedHeaders(List.of(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "Origin",
                                "X-Requested-With"));

                config.setExposedHeaders(List.of(
                                "Authorization"));

                config.setAllowCredentials(true);

                // Cache del preflight durante 1 hora
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", config);

                return source;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthFilter jwtAuthFilter,
                        CorsConfigurationSource corsConfigurationSource) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                // Usar explícitamente nuestra configuración CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                // Preflight CORS
                                                .requestMatchers(HttpMethod.OPTIONS, "/**")
                                                .permitAll()

                                                // Login
                                                .requestMatchers("/api/auth/login")
                                                .permitAll()

                                                // Branding y rutas públicas
                                                .requestMatchers("/api/public/**")
                                                .permitAll()

                                                // IA
                                                .requestMatchers("/api/ai/**")
                                                .permitAll()

                                                // Health / actuator
                                                .requestMatchers("/actuator/**")
                                                .permitAll()

                                                // Administrador
                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMIN")

                                                // Todo lo demás necesita JWT
                                                .anyRequest()
                                                .authenticated())

                                .addFilterBefore(
                                                jwtAuthFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}