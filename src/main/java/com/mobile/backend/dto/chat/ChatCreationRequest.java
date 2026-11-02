package com.mobile.backend.dto.chat;

import com.mobile.backend.entity.Chat.ChatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatCreationRequest {
    
    @NotEmpty(message = "Participant IDs cannot be empty")
    private List<UUID> participantIds;
    
    private String chatName; // Optional, for group chats
    
    @NotBlank(message = "Chat type is required")
    private String chatType; // "SINGLE" or "GROUP"
    
    /**
     * Converts string chatType to ChatType enum
     * @return ChatType enum
     */
    public ChatType getChatTypeEnum() {
        try {
            return ChatType.valueOf(chatType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid chat type: " + chatType);
        }
    }
}