package com.mobile.backend.dto.user;

public record UserResponse(
  Long id,
  String email,
  String username
) {}
