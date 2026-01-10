package com.mobile.backend.controller;

import com.mobile.backend.dto.chat.ChatMessageRequest;
import com.mobile.backend.dto.chat.ChatMessageResponse;
import com.mobile.backend.entity.Message;
import com.mobile.backend.security.CustomUserDetails;
import com.mobile.backend.service.ChatService;
import com.mobile.backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing messages
 * Provides HTTP fallback for sending messages when WebSocket is not available
 */
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    private final ChatService chatService;

    /**
     * Sends a message to a chat (HTTP fallback)
     * POST /api/chats/{chatId}/messages
     * @param chatId ID of the chat
     * @param request Message content
     * @return Sent message details
     */
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody ChatMessageRequest request) {
        
        try {
            // Get authenticated user ID
            Long senderId = getAuthenticatedUserId();
            
            // Verify user is participant in the chat
            if (!chatService.isUserInChat(chatId, senderId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
            }
            
            // Verify chatId in request matches path variable
            if (!request.getChatId().equals(chatId)) {
                return ResponseEntity.badRequest().build();
            }
            
            // Save the message
            Message message = messageService.saveMessage(
                chatId,
                senderId,
                request.getContent()
            );
            
            // Convert to response DTO
            ChatMessageResponse response = ChatMessageResponse.fromEntity(message);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets a specific message by ID
     * GET /api/chats/{chatId}/messages/{messageId}
     * @param chatId ID of the chat
     * @param messageId ID of the message
     * @return Message details
     */
    @GetMapping("/{chatId}/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> getMessage(
            @PathVariable Long chatId,
            @PathVariable Long messageId) {
        
        try {
            Long userId = getAuthenticatedUserId();
            
            // Verify user has access to this chat
            if (!chatService.isUserInChat(chatId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Get the message 
            Message message = messageService.getMessageById(messageId);
            
            // Verify message belongs to the specified chat
            if (!message.getChat().getId().equals(chatId)) {
                return ResponseEntity.notFound().build();
            }
            
            ChatMessageResponse response = ChatMessageResponse.fromEntity(message);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a message (soft delete or hard delete based on your requirements)
     * DELETE /api/chats/{chatId}/messages/{messageId}
     * @param chatId ID of the chat
     * @param messageId ID of the message to delete
     * @return Success response
     */
    @DeleteMapping("/{chatId}/messages/{messageId}")
    public ResponseEntity<DeleteMessageResponse> deleteMessage(
            @PathVariable Long chatId,
            @PathVariable Long messageId) {
        
        try {
            Long userId = getAuthenticatedUserId();
            
            // Verify user has access to this chat
            if (!chatService.isUserInChat(chatId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Delete the message (only if user is the sender)
            messageService.deleteMessage(messageId, userId);
            
            DeleteMessageResponse response = DeleteMessageResponse.builder()
                .messageId(messageId)
                .chatId(chatId)
                .deleted(true)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(DeleteMessageResponse.builder()
                    .messageId(messageId)
                    .chatId(chatId)
                    .deleted(false)
                    .build());
        }
    }

    /**
     * Updates message status (e.g., mark as edited)
     * PATCH /api/chats/{chatId}/messages/{messageId}
     * @param chatId ID of the chat
     * @param messageId ID of the message
     * @param request Update request with new content
     * @return Updated message
     */
    @PatchMapping("/{chatId}/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> updateMessage(
            @PathVariable Long chatId,
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateMessageRequest request) {
        
        try {
            Long userId = getAuthenticatedUserId();
            
            // Verify user has access to this chat
            if (!chatService.isUserInChat(chatId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Update the message 
            Message updatedMessage = messageService.updateMessage(messageId, userId, request.getContent());
            
            ChatMessageResponse response = ChatMessageResponse.fromEntity(updatedMessage);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

   private Long getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new IllegalStateException("User is not authenticated");
    }
    
    Object principal = authentication.getPrincipal();
    
    if (principal instanceof CustomUserDetails) {
        return ((CustomUserDetails) principal).getId();
    }
    
    throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }

    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class UpdateMessageRequest {
        @jakarta.validation.constraints.NotBlank(message = "Content cannot be empty")
        private String content;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeleteMessageResponse {
        private Long messageId;
        private Long chatId;
        private Boolean deleted;
    }
}