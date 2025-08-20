package com.likelion.picklbe.domain.history.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.chatbot.entity.Message;

public interface HistoryMessageRepository extends JpaRepository<Message, Long> {

  // Message.conversationId (Long) 컬럼 기준으로 오름차순 전체 조회
  List<Message> findByConversationIdOrderByIdAsc(Long conversationId);
}
