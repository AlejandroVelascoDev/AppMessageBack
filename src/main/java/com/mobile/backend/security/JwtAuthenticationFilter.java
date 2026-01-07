package com.mobile.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;

  public JwtAuthenticationFilter(
    JwtService jwtService,
    CustomUserDetailsService userDetailsService
  ) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return path.startsWith("/api/auth")
        || path.startsWith("/api/health")
        || path.startsWith("/ws")
        || path.equals("/error");
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {

    try {
      String jwt = extractJwtFromRequest(request);

      if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        authenticateUser(jwt, request);
      }
    } catch (Exception e) {
      logger.error("Cannot set user authentication: {}", e);
    }

    filterChain.doFilter(request, response);
  }

  private String extractJwtFromRequest(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    
    return null;
  }

  private void authenticateUser(String jwt, HttpServletRequest request) {
    String email = jwtService.extractEmail(jwt);

    if (email != null && jwtService.isTokenValid(jwt)) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.getAuthorities()
        );

      authToken.setDetails(
        new WebAuthenticationDetailsSource().buildDetails(request)
      );

      SecurityContextHolder.getContext().setAuthentication(authToken);
    }
  }
}