package com.likelion.picklbe.domain.chatbot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.chatbot.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
  Optional<Conversation> findByIdAndUserId(Long id, Long userId);
}
