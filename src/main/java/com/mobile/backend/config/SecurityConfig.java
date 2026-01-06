package com.mobile.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
    .authorizeHttpRequests(auth -> auth
      .requestMatchers("/health").permitAll()
      .requestMatchers("/auth/**").permitAll()
      .anyRequest().authenticated()
    );

  return http.build();
}
}
