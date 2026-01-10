package com.mobile.backend.dto.chat;

import com.mobile.backend.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for message information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime timestamp;
    
    /**
     * Converts Message entity to ChatMessageResponse DTO
     * @param message Message entity
     * @return ChatMessageResponse DTO
     */
    public static ChatMessageResponse fromEntity(Message message) {
        return ChatMessageResponse.builder()
            .id(message.getId())
            .chatId(message.getChat().getId())
            .senderId(message.getSender().getId())
            .senderUsername(message.getSender().getUsername())
            .content(message.getContent())
            .timestamp(message.getTimestamp())
            .build();
    }
}