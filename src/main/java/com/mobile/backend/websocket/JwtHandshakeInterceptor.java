package com.mobile.backend.websocket;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.mobile.backend.security.JwtService;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);
  private final JwtService jwtService;

  public JwtHandshakeInterceptor(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes
  ) {
    logger.info(" WebSocket Handshake Started ");
    logger.info("URI: {}", request.getURI());
    
    String query = request.getURI().getQuery();
    logger.info("Query String: {}", query);

    if (query == null || !query.startsWith("token=")) {
      logger.error(" Token not found in query string");
      return false;
    }

    String token = query.substring(6);
    logger.info("Token : {}...", token.substring(0, Math.min(20, token.length())));

    try {
      if (!jwtService.isTokenValid(token)) {
        logger.error(" Token expired");
        return false;
      }

      String email = jwtService.extractEmail(token);
      logger.info(" Token {}", email);
      
      attributes.put("email", email);
      logger.info(" WebSocket Handshake Success ");
      
      return true;
    } catch (Exception e) {
      logger.error(" Error : {}", e.getMessage());
      return false;
    }
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception
  ) {
    if (exception != null) {
      logger.error("Error en afterHandshake: {}", exception.getMessage());
    } else {
      logger.info("AfterHandshake completed successfully");
    }
  }
}