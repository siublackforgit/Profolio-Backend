package com.rickyyeung.profolio.filter;

import com.rickyyeung.profolio.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Added this
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter; // Added this

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// MUST extend OncePerRequestFilter
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if ("/auth/refresh".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> "accessToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // Fix for "cannot resolve validateToken":
        // Ensure your JwtUtils.java has a method named exactly 'validateToken'
        if (token != null) {
            if (jwtUtils.validateToken(token)) {
                // ... set security context ...
                filterChain.doFilter(request, response);
            } else {
                // Token is expired or invalid
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Session expired. Please log in again.\"}");
                // We do NOT call filterChain.doFilter because we want to block the request
            }
        } else {
            filterChain.doFilter(request, response);
        }

    }
}