package com.mobile.backend.controller;

import com.mobile.backend.dto.chat.ChatCreationRequest;
import com.mobile.backend.dto.chat.ChatMessageResponse;
import com.mobile.backend.dto.chat.ChatResponse;
import com.mobile.backend.entity.Chat;
import com.mobile.backend.entity.Message;
import com.mobile.backend.security.CustomUserDetails;
import com.mobile.backend.service.ChatService;
import com.mobile.backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing chats
 */
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final MessageService messageService;

    /**
     * Creates a new chat
     * POST /api/chats
     * @param request ChatCreationRequest with participant IDs, name, and type
     * @return Created chat information
     */
    @PostMapping
    public ResponseEntity<ChatResponse> createChat(@Valid @RequestBody ChatCreationRequest request) {
        try {
            // Create the chat
            Chat chat = chatService.createChat(
                request.getParticipantIds(),
                request.getChatName(),
                request.getChatTypeEnum()
            );
            
            // Get last message (will be null for new chat)
            Message lastMessage = messageService.getLastMessage(chat.getId());
            
            // Convert to response DTO
            ChatResponse response = ChatResponse.fromEntity(chat, lastMessage);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lists all chats for the authenticated user
     * GET /api/chats
     * @return List of user's chats with last message
     */
    @GetMapping
    public ResponseEntity<List<ChatResponse>> getUserChats() {
        try {
            // Get authenticated user ID
            Long userId = getAuthenticatedUserId();
            
            // Fetch user's chats
            List<Chat> chats = chatService.getChatsByUserId(userId);
            
            // Convert to response DTOs with last messages
            List<ChatResponse> responses = chats.stream()
                .map(chat -> {
                    Message lastMessage = messageService.getLastMessage(chat.getId());
                    return ChatResponse.fromEntity(chat, lastMessage);
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets details of a specific chat
     * GET /api/chats/{chatId}
     * @param chatId ID of the chat
     * @return Chat details with participants and last message
     */
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChatById(@PathVariable Long chatId) {
        try {
            // Verify user has access to this chat
            Long userId = getAuthenticatedUserId();
            if (!chatService.isUserInChat(chatId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Fetch chat
            Chat chat = chatService.getChatById(chatId);
            Message lastMessage = messageService.getLastMessage(chatId);
            
            // Convert to response DTO
            ChatResponse response = ChatResponse.fromEntity(chat, lastMessage);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Gets message history for a chat with pagination
     * GET /api/chats/{chatId}/messages
     * @param chatId ID of the chat
     * @param page Page number (default: 0)
     * @param size Page size (default: 50)
     * @return Paginated list of messages
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<MessageHistoryResponse> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            // Verify user has access to this chat
            Long userId = getAuthenticatedUserId();
            if (!chatService.isUserInChat(chatId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Fetch paginated messages
            Page<Message> messagePage = messageService.getMessagesByChatIdPaginated(chatId, page, size);
            
            // Convert to response DTOs
            List<ChatMessageResponse> messageResponses = messagePage.getContent().stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
            
            // Build response with pagination info
            MessageHistoryResponse response = MessageHistoryResponse.builder()
                .chatId(chatId)
                .messages(messageResponses)
                .currentPage(page)
                .pageSize(size)
                .totalPages(messagePage.getTotalPages())
                .totalMessages(messagePage.getTotalElements())
                .hasMore(messagePage.hasNext())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Marks messages as read in a chat
     * PUT /api/chats/{chatId}/read
     * @param chatId ID of the chat
     * @return Number of messages marked as read
     */
    @PutMapping("/{chatId}/read")
    public ResponseEntity<ReadStatusResponse> markMessagesAsRead(@PathVariable Long chatId) {
        try {
            Long userId = getAuthenticatedUserId();
            
            int messagesRead = messageService.markMessagesAsRead(chatId, userId);
            
            ReadStatusResponse response = ReadStatusResponse.builder()
                .chatId(chatId)
                .messagesMarkedAsRead(messagesRead)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets the count of unread messages in a chat
     * GET /api/chats/{chatId}/unread-count
     * @param chatId ID of the chat
     * @return Count of unread messages
     */
    @GetMapping("/{chatId}/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@PathVariable Long chatId) {
        try {
            Long userId = getAuthenticatedUserId();
            
            if (!chatService.isUserInChat(chatId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            long unreadCount = messageService.getUnreadMessageCount(chatId, userId);
            
            UnreadCountResponse response = UnreadCountResponse.builder()
                .chatId(chatId)
                .unreadCount(unreadCount)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Helper method to get authenticated user ID from security context
     * @return User ID of authenticated user
     */
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
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MessageHistoryResponse {
        private Long chatId;
        private List<ChatMessageResponse> messages;
        private Integer currentPage;
        private Integer pageSize;
        private Integer totalPages;
        private Long totalMessages;
        private Boolean hasMore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReadStatusResponse {
        private Long chatId;
        private Integer messagesMarkedAsRead;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UnreadCountResponse {
        private Long chatId;
        private Long unreadCount;
    }
}