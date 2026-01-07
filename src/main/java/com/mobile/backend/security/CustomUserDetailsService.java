package com.mobile.backend.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.mobile.backend.repository.UserRepository;

@Service
public class CustomUserDetailsService
  implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(
    UserRepository userRepository
  ) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email)
    throws UsernameNotFoundException {

    var user = userRepository.findByEmail(email)
      .orElseThrow(() ->
        new UsernameNotFoundException("User not found")
      );

    return User
      .withUsername(user.getEmail())
      .password(user.getPassword())
      .authorities("USER")
      .build();
  }
}