package com.techup.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Lazy UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

@Override
protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwt;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    jwt = authHeader.substring(7);

    try {
        Long userId = jwtService.getUserId(jwt); // ดึงจาก token
        String email = jwtService.extractEmail(jwt);

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load UserDetails
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

            // สร้าง UsernamePasswordAuthenticationToken
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // เก็บ userId ใน principal (cast ใน CustomUserDetails ถ้าต้องการ)
            // หรือเก็บไว้ใน details map
            authToken.setDetails(userId);

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

    } catch (Exception e) {
        logger.debug("JWT token validation failed: " + e.getMessage());
    }

    filterChain.doFilter(request, response);
}
}