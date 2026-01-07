package com.mobile.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobile.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByUsername(String username);
  List<User> findByUsernameContainingIgnoreCase(String username);
}
