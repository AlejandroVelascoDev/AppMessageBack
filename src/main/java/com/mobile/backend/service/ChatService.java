package com.mobile.backend.service;

import com.mobile.backend.entity.Chat;
import com.mobile.backend.entity.Chat.ChatType;
import com.mobile.backend.entity.User;
import com.mobile.backend.repository.ChatRepository;
import com.mobile.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new chat with the specified participants
     * @param userIds List of user IDs to add as participants
     * @param chatName Name of the chat (optional, used for group chats)
     * @param type Type of chat (SINGLE or GROUP)
     * @return The created Chat entity
     * @throws IllegalArgumentException if users are not found or invalid chat type
     */
    @Transactional
    public Chat createChat(List<Long> userIds, String chatName, ChatType type) {
        // Validate input
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("User IDs list cannot be empty");
        }

        // For single chats, ensure exactly 2 participants
        if (type == ChatType.SINGLE && userIds.size() != 2) {
            throw new IllegalArgumentException("Single chat must have exactly 2 participants");
        }

        // Check if single chat already exists between these users
        if (type == ChatType.SINGLE) {
            var existingChat = chatRepository.findSingleChatBetweenUsers(userIds.get(0), userIds.get(1));
            if (existingChat.isPresent()) {
                return existingChat.get();
            }
        }

        // Fetch all users from database
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new IllegalArgumentException("One or more users not found");
        }

        // Create new chat entity
        Chat chat = new Chat();
        chat.setName(chatName);
        chat.setType(type);
        chat.setParticipants(new HashSet<>(users));

        // Save and return
        return chatRepository.save(chat);
    }

    /**
     * Retrieves all chats where the user is a participant
     * @param userId ID of the user
     * @return List of chats ordered by last update
     */
    @Transactional(readOnly = true)
    public List<Chat> getChatsByUserId(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        // Return chats ordered by most recent activity
        return chatRepository.findChatsByUserIdOrderByUpdatedAtDesc(userId);
    }

    /**
     * Retrieves a specific chat by its ID
     * @param chatId ID of the chat
     * @return The Chat entity
     * @throws IllegalArgumentException if chat is not found
     */
    @Transactional(readOnly = true)
    public Chat getChatById(Long chatId) {
        return chatRepository.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("Chat not found with ID: " + chatId));
    }

    /**
     * Verifies if a user is a participant in a specific chat
     * @param chatId ID of the chat
     * @param userId ID of the user
     * @return true if user is participant, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isUserInChat(Long chatId, Long userId) {
        return chatRepository.isUserParticipant(chatId, userId);
    }

    /**
     * Adds a new participant to a group chat
     * @param chatId ID of the chat
     * @param userId ID of the user to add
     * @throws IllegalArgumentException if chat is not a group or user/chat not found
     */
    @Transactional
    public void addParticipant(Long chatId, Long userId) {
        Chat chat = getChatById(chatId);
        
        // Only allow adding participants to group chats
        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalArgumentException("Can only add participants to group chats");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        chat.getParticipants().add(user);
        chatRepository.save(chat);
    }

    /**
     * Removes a participant from a group chat
     * @param chatId ID of the chat
     * @param userId ID of the user to remove
     */
    @Transactional
    public void removeParticipant(Long chatId, Long userId) {
        Chat chat = getChatById(chatId);
        
        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalArgumentException("Can only remove participants from group chats");
        }

        chat.getParticipants().removeIf(user -> user.getId().equals(userId));
        chatRepository.save(chat);
    }
}