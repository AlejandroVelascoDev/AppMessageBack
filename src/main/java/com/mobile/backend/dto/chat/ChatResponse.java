package com.mobile.backend.dto.chat;

import com.mobile.backend.entity.Chat;
import com.mobile.backend.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO containing chat information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    
    private Long id;
    private String name;
    private String type;
    private ChatMessageResponse lastMessage;
    private List<ParticipantInfo> participants;
    
    /**
     * Converts Chat entity to ChatResponse DTO
     * @param chat Chat entity
     * @param lastMessage Last message (can be null)
     * @return ChatResponse DTO
     */
    public static ChatResponse fromEntity(Chat chat, Message lastMessage) {
        return ChatResponse.builder()
            .id(chat.getId())
            .name(chat.getName())
            .type(chat.getType().name())
            .lastMessage(lastMessage != null ? ChatMessageResponse.fromEntity(lastMessage) : null)
            .participants(chat.getParticipants().stream()
                .map(ParticipantInfo::fromEntity)
                .collect(Collectors.toList()))
            .build();
    }
    
    /**
     * Inner class for basic participant information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantInfo {
        private Long id;
        private String username;
        private String email;
        
        public static ParticipantInfo fromEntity(com.mobile.backend.entity.User user) {
            return ParticipantInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        }
    }
}
