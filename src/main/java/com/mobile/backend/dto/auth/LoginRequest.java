package com.mobile.backend.dto.auth;

public record LoginRequest(
  String email,
  String password
) {}
