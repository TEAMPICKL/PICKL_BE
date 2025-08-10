package com.likelion.picklbe.domain.chatbot.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.chatbot.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

  List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
}
