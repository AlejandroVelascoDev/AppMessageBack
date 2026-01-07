package com.mobile.backend.controller;

import org.springframework.web.bind.annotation.*;

import com.mobile.backend.dto.auth.*;
import com.mobile.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public AuthResponse register(
    @RequestBody RegisterRequest request
  ) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public AuthResponse login(
    @RequestBody LoginRequest request
  ) {
    return authService.login(request);
  }
}