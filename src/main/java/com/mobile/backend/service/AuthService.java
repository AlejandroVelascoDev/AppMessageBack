package com.mobile.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobile.backend.dto.auth.*;
import com.mobile.backend.entity.User;
import com.mobile.backend.repository.UserRepository;
import com.mobile.backend.security.JwtService;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already registered");
    }

    User user = new User();
    user.setEmail(request.email());
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));

    userRepository.save(user);

    String token = jwtService.generateToken(user.getEmail());
    return new AuthResponse(token);
  }

  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.email())
      .orElseThrow(() -> new RuntimeException("Invalid credentials"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new RuntimeException("Invalid credentials");
    }

    String token = jwtService.generateToken(user.getEmail());
    return new AuthResponse(token);
  }
}