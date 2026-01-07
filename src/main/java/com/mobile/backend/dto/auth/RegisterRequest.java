package com.mobile.backend.dto.auth;

public record RegisterRequest(
  String email,
  String password,
  String username
) {}