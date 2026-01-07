package com.mobile.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.mobile.backend.dto.user.UserResponse;
import com.mobile.backend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
    String email = authentication.getName();
    UserResponse user = userService.getUserByEmail(email);
    return ResponseEntity.ok(user);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    UserResponse user = userService.getUserById(id);
    return ResponseEntity.ok(user);
  }

  @GetMapping("/search")
  public ResponseEntity<List<UserResponse>> searchUsers(
    @RequestParam(required = false) String username,
    @RequestParam(required = false) String email
  ) {
    List<UserResponse> users = userService.searchUsers(username, email);
    return ResponseEntity.ok(users);
  }

  @PutMapping("/me")
  public ResponseEntity<UserResponse> updateCurrentUser(
    Authentication authentication,
    @RequestBody com.mobile.backend.service.UserService.UpdateUserRequest request
  ) {
    String email = authentication.getName();
    UserResponse user = userService.updateUser(email, request);
    return ResponseEntity.ok(user);
  }

  @DeleteMapping("/me")
  public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
    String email = authentication.getName();
    userService.deleteUser(email);
    return ResponseEntity.noContent().build();
  }

  public record UpdateUserRequest(String username) {}
}