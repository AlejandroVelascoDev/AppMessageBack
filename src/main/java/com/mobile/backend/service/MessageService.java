package com.mobile.backend.service;

import com.mobile.backend.entity.Chat;
import com.mobile.backend.entity.Message;
import com.mobile.backend.entity.Message.MessageStatus;
import com.mobile.backend.entity.Message.MessageType;
import com.mobile.backend.entity.User;
import com.mobile.backend.repository.ChatRepository;
import com.mobile.backend.repository.MessageRepository;
import com.mobile.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    /**
     * Saves a new message to the database
     * @param chatId ID of the chat where the message will be sent
     * @param senderId ID of the user sending the message
     * @param content Text content of the message
     * @return The saved Message entity
     * @throws IllegalArgumentException if chat/user not found or user is not a participant
     */
    @Transactional
    public Message saveMessage(Long chatId, Long senderId, String content) {
        // Validate input
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        // Verify chat exists
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("Chat not found with ID: " + chatId));

        // Verify sender exists
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + senderId));

        // Verify sender is a participant in the chat
        if (!chatService.isUserInChat(chatId, senderId)) {
            throw new IllegalArgumentException("User is not a participant in this chat");
        }

        // Create new message entity
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content.trim());
        message.setMessageType(MessageType.TEXT);
        message.setStatus(MessageStatus.SENT);

        // Save and return
        return messageRepository.save(message);
    }

    /**
     * Saves a message with a specific type (TEXT, IMAGE, FILE, etc.)
     * @param chatId ID of the chat
     * @param senderId ID of the sender
     * @param content Message content or file URL
     * @param messageType Type of message
     * @return The saved Message entity
     */
    @Transactional
    public Message saveMessage(Long chatId, Long senderId, String content, MessageType messageType) {
        Message message = saveMessage(chatId, senderId, content);
        message.setMessageType(messageType);
        return messageRepository.save(message);
    }

    /**
     * Retrieves all messages for a specific chat ordered by timestamp
     * @param chatId ID of the chat
     * @return List of messages in chronological order
     * @throws IllegalArgumentException if chat not found
     */
    @Transactional(readOnly = true)
    public List<Message> getMessagesByChatId(Long chatId) {
        // Verify chat exists
        if (!chatRepository.existsById(chatId)) {
            throw new IllegalArgumentException("Chat not found with ID: " + chatId);
        }

        // Return messages ordered by timestamp (oldest first)
        return messageRepository.findByChatIdOrderByTimestampAsc(chatId);
    }

    /**
     * Retrieves paginated messages for a chat
     * @param chatId ID of the chat
     * @param page Page number (0-indexed)
     * @param size Number of messages per page
     * @return Page of messages ordered by most recent first
     */
    @Transactional(readOnly = true)
    public Page<Message> getMessagesByChatIdPaginated(Long chatId, int page, int size) {
        if (!chatRepository.existsById(chatId)) {
            throw new IllegalArgumentException("Chat not found with ID: " + chatId);
        }

        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByChatIdOrderByTimestampDesc(chatId, pageable);
    }

    /**
     * Marks all unread messages in a chat as read for a specific user
     * @param chatId ID of the chat
     * @param userId ID of the user reading the messages
     * @return Number of messages marked as read
     */
    @Transactional
    public int markMessagesAsRead(Long chatId, Long userId) {
        // Verify user is participant
        if (!chatService.isUserInChat(chatId, userId)) {
            throw new IllegalArgumentException("User is not a participant in this chat");
        }

        return messageRepository.markMessagesAsRead(chatId, userId);
    }

    /**
     * Gets the count of unread messages in a chat for a user
     * @param chatId ID of the chat
     * @param userId ID of the user
     * @return Number of unread messages
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long chatId, Long userId) {
        return messageRepository.countUnreadMessages(chatId, userId);
    }

    /**
     * Retrieves the last message sent in a chat
     * @param chatId ID of the chat
     * @return The most recent Message, or null if no messages exist
     */
    @Transactional(readOnly = true)
    public Message getLastMessage(Long chatId) {
        return messageRepository.findLastMessageByChatId(chatId);
    }

    /**
     * Searches for messages containing specific text in a chat
     * @param chatId ID of the chat
     * @param searchTerm Text to search for
     * @return List of messages containing the search term
     */
    @Transactional(readOnly = true)
    public List<Message> searchMessages(Long chatId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty");
        }

        return messageRepository.searchMessagesInChat(chatId, searchTerm.trim());
    }

    /**
     * Deletes a specific message
     * @param messageId ID of the message to delete
     * @param userId ID of the user attempting to delete (must be the sender)
     * @throws IllegalArgumentException if user is not the sender
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        // Only allow sender to delete their own messages
        if (!message.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the sender can delete this message");
        }

        messageRepository.delete(message);
    }

    public Message getMessageById(Long messageId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMessageById'");
    }

    public Message updateMessage(Long messageId, Long userId, String content) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateMessage'");
    }
}