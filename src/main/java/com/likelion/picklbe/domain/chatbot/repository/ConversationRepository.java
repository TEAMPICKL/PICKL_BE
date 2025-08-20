package com.likelion.picklbe.domain.chatbot.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.chatbot.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  /** 사용자별 세션 개수 */
  long countByUserId(Long userId);

  /** 사용자별 세션 페이징 (정렬은 Pageable에서 지정: 기본 modifiedAt DESC) */
  Page<Conversation> findByUserId(Long userId, Pageable pageable);

  /** 소유권 검증용 조회 */
  Optional<Conversation> findByIdAndUserId(Long id, Long userId);
}
