package com.mobile.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.mobile.backend.websocket.ChatWebSocketHandler;
import com.mobile.backend.websocket.JwtHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final ChatWebSocketHandler chatHandler;
  private final JwtHandshakeInterceptor jwtInterceptor;

  public WebSocketConfig(
      ChatWebSocketHandler chatHandler,
      JwtHandshakeInterceptor jwtInterceptor
  ) {
    this.chatHandler = chatHandler;
    this.jwtInterceptor = jwtInterceptor;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(chatHandler, "/ws/chat")
        .addInterceptors(jwtInterceptor)
        .setAllowedOrigins("*"); 
        
  }
}