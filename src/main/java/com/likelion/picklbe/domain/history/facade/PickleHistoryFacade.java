package com.likelion.picklbe.domain.history.facade;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.chatbot.repository.ConversationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PickleHistoryFacade {

  private final ConversationRepository conversationRepo;

  /** 레거시 대체: 피클히스토리 개수 = 내 대화 개수 */
  public int countByUserId(Long userId) {
    long cnt = conversationRepo.countByUserId(userId);
    return (int) Math.min(cnt, Integer.MAX_VALUE);
  }
}
