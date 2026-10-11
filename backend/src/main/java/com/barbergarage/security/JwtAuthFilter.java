package com.barbergarage.security;

import com.barbergarage.repo.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    /*
     * No aplicar el filtro JWT a:
     * - Preflight CORS (OPTIONS)
     * - Login
     * - Rutas públicas
     * - Servicio IA público
     * - Actuator
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        return path.equals("/api/auth/login")
                || path.equals("/api/public")
                || path.startsWith("/api/public/")
                || path.equals("/api/ai")
                || path.startsWith("/api/ai/")
                || path.equals("/actuator")
                || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain) throws ServletException, IOException {

        String authorizationHeader = req.getHeader("Authorization");

        /*
         * Si no existe Bearer Token simplemente continúa.
         * Spring Security decidirá posteriormente si la ruta
         * necesita autenticación.
         */
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            chain.doFilter(req, res);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            if (jwt.valid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String username = jwt.username(token);

                users.findByUsernameIgnoreCase(username).ifPresent(user -> {

                    var authority = new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().name());

                    var authentication = new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            List.of(authority));

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                });
            }

        } catch (Exception e) {

            /*
             * Token inválido:
             * no interrumpimos aquí la solicitud.
             * Spring Security devolverá 401/403 si la ruta
             * realmente requiere autenticación.
             */
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }
}