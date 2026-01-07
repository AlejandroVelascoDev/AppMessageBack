package com.mobile.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobile.backend.dto.user.UserResponse;
import com.mobile.backend.entity.User;
import com.mobile.backend.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserResponse getUserByEmail(String email) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("User not found"));
    
    return mapToResponse(user);
  }

  public UserResponse getUserById(Long id) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("User not found"));
    
    return mapToResponse(user);
  }

  public List<UserResponse> searchUsers(String username, String email) {
    List<User> users = new ArrayList<>();
    
    if (username != null && !username.isEmpty()) {
      users = userRepository.findByUsernameContainingIgnoreCase(username);
    } else if (email != null && !email.isEmpty()) {
      userRepository.findByEmail(email).ifPresent(users::add);
    } else {
      users = userRepository.findAll();
    }
    
    return users.stream()
      .map(this::mapToResponse)
      .toList();
  }

  @Transactional
  public UserResponse updateUser(String email, UpdateUserRequest request) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("User not found"));
    
    if (request.username() != null && !request.username().isEmpty()) {
      if (userRepository.existsByUsername(request.username()) 
          && !user.getUsername().equals(request.username())) {
        throw new IllegalArgumentException("Username already taken");
      }
      user.setUsername(request.username());
    }
    
    userRepository.save(user);
    return mapToResponse(user);
  }

  @Transactional
  public void deleteUser(String email) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("User not found"));
    
    userRepository.delete(user);
  }

  private UserResponse mapToResponse(User user) {
    return new UserResponse(
      user.getId(),
      user.getEmail(),
      user.getUsername()
    );
  }

  public record UpdateUserRequest(String username) {}
}