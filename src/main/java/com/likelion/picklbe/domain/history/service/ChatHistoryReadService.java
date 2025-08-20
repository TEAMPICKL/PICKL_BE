package com.likelion.picklbe.domain.history.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.likelion.picklbe.domain.chatbot.entity.Conversation;
import com.likelion.picklbe.domain.chatbot.repository.ConversationRepository;
import com.likelion.picklbe.domain.history.dto.MessageDto;
import com.likelion.picklbe.domain.history.dto.SessionSummaryDto;
import com.likelion.picklbe.domain.history.repository.HistoryMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatHistoryReadService {

  private final ConversationRepository conversationRepo;
  private final HistoryMessageRepository messageRepo;

  // 세션 존재 + 소유권 확인
  private Conversation owned(Long sessionId, Long me) {
    return conversationRepo
        .findByIdAndUserId(sessionId, me)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "세션 없음 또는 권한 없음"));
  }

  // 세션 목록 (페이지네이션, 기본 정렬: modifiedAt DESC)
  public Page<SessionSummaryDto> list(Long me, Pageable pageable) {
    Sort sort =
        pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Order.desc("modifiedAt"));
    Pageable fixed = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

    return conversationRepo
        .findByUserId(me, fixed)
        .map(c -> new SessionSummaryDto(c.getId(), c.getTitle(), c.getModifiedAt()));
  }

  // 세션 개수
  public long count(Long me) {
    return conversationRepo.countByUserId(me);
  }

  // 특정 세션의 모든 메시지(오름차순) 조회
  public List<MessageDto> messages(Long me, Long sessionId) {
    owned(sessionId, me); // 소유권만 확인
    return messageRepo.findByConversationIdOrderByIdAsc(sessionId).stream()
        .map(
            m ->
                new MessageDto(
                    m.getId(),
                    m.getRole() != null ? m.getRole().name() : null,
                    m.getContent(),
                    m.getCreatedAt()))
        .toList();
  }
}
