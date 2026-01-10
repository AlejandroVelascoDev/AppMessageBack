package com.mobile.backend.repository;

import com.mobile.backend.entity.Chat;
import com.mobile.backend.entity.Chat.ChatType;
import com.mobile.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    // search chats by type 
    List<Chat> findByType(ChatType type);
    
    // search types when the user is a participant
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.id = :userId")
    List<Chat> findChatsByUserId(@Param("userId") Long userId);
    
    // search chat between two users
    @Query("SELECT c FROM Chat c JOIN c.participants p1 JOIN c.participants p2 " +
           "WHERE c.type = 'SINGLE' AND p1.id = :user1Id AND p2.id = :user2Id")
    Optional<Chat> findSingleChatBetweenUsers(
        @Param("user1Id") Long user1Id, 
        @Param("user2Id") Long user2Id
    );
    
    // search groupal chat by name
    List<Chat> findByNameContainingIgnoreCase(String name);
    
   // Search for chats where a user is a participant, sorted by last update
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.id = :userId ORDER BY c.updatedAt DESC")
    List<Chat> findChatsByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);
    
    //  verify if a user is a participant of a chat
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Chat c JOIN c.participants p WHERE c.id = :chatId AND p.id = :userId")
    boolean isUserParticipant(@Param("chatId") Long chatId, @Param("userId") Long userId);
}