package com.mobile.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mobile.backend.security.CustomUserDetailsService;
import com.mobile.backend.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
    this.jwtFilter = jwtFilter;
  }

  @Bean
  public AuthenticationProvider authenticationProvider(
    CustomUserDetailsService userDetailsService,
    PasswordEncoder passwordEncoder
  ) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration config
  ) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .cors(Customizer.withDefaults())
      .csrf(csrf -> csrf.disable())
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .formLogin(form -> form.disable())
      .httpBasic(basic -> basic.disable())
      .exceptionHandling(exception -> exception
        .authenticationEntryPoint((req, res, ex) -> {
          res.setContentType("application/json");
          res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          res.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + 
            ex.getMessage() + "\"}");
        })
      )
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/api/health",
          "/api/auth/**",
          "/ws/**",
          "/error"
        ).permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}