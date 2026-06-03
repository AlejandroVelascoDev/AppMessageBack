package com.mobile.backend.config;

import com.mobile.backend.entity.Chat;
import com.mobile.backend.entity.Message;
import com.mobile.backend.entity.User;
import com.mobile.backend.repository.ChatRepository;
import com.mobile.backend.repository.MessageRepository;
import com.mobile.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedDatabase(
            UserRepository userRepository,
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return; // Already seeded
            }

            // --- Users ---
            User alice = new User();
            alice.setUsername("alice");
            alice.setEmail("alice@example.com");
            alice.setPassword(passwordEncoder.encode("password123"));

            User bob = new User();
            bob.setUsername("bob");
            bob.setEmail("bob@example.com");
            bob.setPassword(passwordEncoder.encode("password123"));

            User charlie = new User();
            charlie.setUsername("charlie");
            charlie.setEmail("charlie@example.com");
            charlie.setPassword(passwordEncoder.encode("password123"));

            userRepository.saveAll(Set.of(alice, bob, charlie));

            // --- Chat directo: alice <-> bob ---
            Chat directChat = new Chat();
            directChat.setType(Chat.ChatType.SINGLE);
            directChat.setParticipants(Set.of(alice, bob));
            chatRepository.save(directChat);

            Message m1 = new Message();
            m1.setChat(directChat);
            m1.setSender(alice);
            m1.setContent("Hola Bob, como estas?");
            m1.setMessageType(Message.MessageType.TEXT);
            m1.setStatus(Message.MessageStatus.READ);

            Message m2 = new Message();
            m2.setChat(directChat);
            m2.setSender(bob);
            m2.setContent("Todo bien Alice! Y tu?");
            m2.setMessageType(Message.MessageType.TEXT);
            m2.setStatus(Message.MessageStatus.READ);

            Message m3 = new Message();
            m3.setChat(directChat);
            m3.setSender(alice);
            m3.setContent("Muy bien, gracias!");
            m3.setMessageType(Message.MessageType.TEXT);
            m3.setStatus(Message.MessageStatus.DELIVERED);

            messageRepository.saveAll(Set.of(m1, m2, m3));

            // --- Chat grupal: alice, bob y charlie ---
            Chat groupChat = new Chat();
            groupChat.setName("Grupo de prueba");
            groupChat.setType(Chat.ChatType.GROUP);
            groupChat.setParticipants(Set.of(alice, bob, charlie));
            chatRepository.save(groupChat);

            Message gm1 = new Message();
            gm1.setChat(groupChat);
            gm1.setSender(charlie);
            gm1.setContent("Bienvenidos al grupo!");
            gm1.setMessageType(Message.MessageType.TEXT);
            gm1.setStatus(Message.MessageStatus.READ);

            Message gm2 = new Message();
            gm2.setChat(groupChat);
            gm2.setSender(alice);
            gm2.setContent("Hola a todos!");
            gm2.setMessageType(Message.MessageType.TEXT);
            gm2.setStatus(Message.MessageStatus.SENT);

            messageRepository.saveAll(Set.of(gm1, gm2));

            System.out.println("[DataInitializer] DB data for test.");
        };
    }
}
