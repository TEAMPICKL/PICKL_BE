package com.likelion.picklbe.domain.chatbot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.chatbot.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  long countByUserId(Long userId);

  Page<Conversation> findByUserId(Long userId, Pageable pageable);

  Optional<Conversation> findByIdAndUserId(Long id, Long userId);

  long deleteByIdAndUserId(Long id, Long userId);

  boolean existsByIdAndUserId(Long id, Long userId);

  List<Conversation> findByUserIdOrderByModifiedAtDesc(Long userId);
}
