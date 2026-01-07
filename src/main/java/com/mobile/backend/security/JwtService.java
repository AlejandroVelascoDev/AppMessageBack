package com.mobile.backend.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  private final SecretKey signingKey;
  private final long expiration;

  public JwtService(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration}") long expiration
  ) {
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.expiration = expiration;
  }

  public String generateToken(String email) {
    return Jwts.builder()
        .subject(email)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(signingKey)
        .compact();
  }

  public String extractEmail(String token) {
    return extractClaims(token).getSubject();
  }

  public boolean isTokenValid(String token) {
    try {
      extractClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public boolean isTokenExpired(String token) {
    try {
      return extractClaims(token).getExpiration().before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      return true;
    }
  }

  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)  
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}