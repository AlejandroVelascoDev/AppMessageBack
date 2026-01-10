package com.mobile.backend.websocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

  private final Set<WebSocketSession> sessions =
      ConcurrentHashMap.newKeySet();

  @Override
  public void afterConnectionEstablished(WebSocketSession session)
      throws Exception {

    sessions.add(session);

    String email = (String) session.getAttributes().get("email");

    session.sendMessage(
        new TextMessage(" Conected has " + email)
    );
  }

  @Override
  protected void handleTextMessage(
      WebSocketSession session,
      TextMessage message
  ) throws Exception {

    String email = (String) session.getAttributes().get("email");
    String payload = email + ": " + message.getPayload();


    for (WebSocketSession s : sessions) {
      if (s.isOpen()) {
        s.sendMessage(new TextMessage(payload));
      }
    }
  }

  @Override
  public void afterConnectionClosed(
      WebSocketSession session,
      CloseStatus status
  ) {
    sessions.remove(session);
  }
}
