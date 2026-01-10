
package com.mobile.backend.repository;

import com.mobile.backend.entity.Message;
import com.mobile.backend.entity.Message.MessageStatus;
import com.mobile.backend.entity.Message.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Buscar mensajes de un chat específico, ordenados por timestamp
    List<Message> findByChatIdOrderByTimestampAsc(Long chatId);
    
    // Buscar mensajes de un chat con paginación
    Page<Message> findByChatIdOrderByTimestampDesc(Long chatId, Pageable pageable);
    
    // Buscar mensajes enviados por un usuario específico
    List<Message> findBySenderIdOrderByTimestampDesc(Long senderId);
    
    // Buscar mensajes por tipo
    List<Message> findByChatIdAndMessageType(Long chatId, MessageType messageType);
    
    // Buscar mensajes por estado
    List<Message> findByChatIdAndStatus(Long chatId, MessageStatus status);
    
    // Contar mensajes no leídos en un chat para un usuario
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId " +
           "AND m.sender.id != :userId AND m.status != 'READ'")
    long countUnreadMessages(@Param("chatId") Long chatId, @Param("userId") Long userId);
    
    // Obtener el último mensaje de un chat
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId " +
           "ORDER BY m.timestamp DESC LIMIT 1")
    Message findLastMessageByChatId(@Param("chatId") Long chatId);
    
    // Buscar mensajes en un rango de fechas
    List<Message> findByChatIdAndTimestampBetween(
        Long chatId, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    // Marcar mensajes como leídos
    @Modifying
    @Query("UPDATE Message m SET m.status = 'READ' " +
           "WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.status != 'READ'")
    int markMessagesAsRead(@Param("chatId") Long chatId, @Param("userId") Long userId);
    
    // Marcar mensajes como entregados
    @Modifying
    @Query("UPDATE Message m SET m.status = 'DELIVERED' " +
           "WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.status = 'SENT'")
    int markMessagesAsDelivered(@Param("chatId") Long chatId, @Param("userId") Long userId);
    
    // Buscar mensajes que contienen un texto específico
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId " +
           "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Message> searchMessagesInChat(@Param("chatId") Long chatId, @Param("searchTerm") String searchTerm);
    
    // Eliminar mensajes antiguos de un chat
    @Modifying
    @Query("DELETE FROM Message m WHERE m.chat.id = :chatId AND m.timestamp < :beforeDate")
    int deleteOldMessages(@Param("chatId") Long chatId, @Param("beforeDate") LocalDateTime beforeDate);
}