package com.likelion.picklbe.domain.chatbot.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatRequest;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatResponse;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ConversationDetailResponse;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.MessageItem;
import com.likelion.picklbe.domain.chatbot.dto.PythonDtos.ChatRes;
import com.likelion.picklbe.domain.chatbot.dto.PythonDtos.HistoryReq;
import com.likelion.picklbe.domain.chatbot.dto.PythonDtos.Turn;
import com.likelion.picklbe.domain.chatbot.entity.Conversation;
import com.likelion.picklbe.domain.chatbot.entity.Message;
import com.likelion.picklbe.domain.chatbot.entity.MessageRole;
import com.likelion.picklbe.domain.chatbot.exception.ChatbotErrorCode;
import com.likelion.picklbe.domain.chatbot.repository.ConversationRepository;
import com.likelion.picklbe.domain.chatbot.repository.MessageRepository;
import com.likelion.picklbe.domain.chatbot.repository.UserMemoryRepository;
import com.likelion.picklbe.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

  private final ConversationRepository conversationRepo;
  private final MessageRepository messageRepo;
  private final UserMemoryRepository memoryRepo;
  private final WebClient langchainWebClient;

  @Value("${chatbot.history-max-turns:20}")
  private int historyMaxTurns;

  @Value("${chatbot.memory-max-rows:50}")
  private int memoryMaxRows;

  @Value("${chatbot.timeout-ms:10000}")
  private long timeoutMs;

  // NEW: 제목 생성 요청/응답 DTO (로컬 레코드)
  private record TitleReq(String message, String memory, int max_len) {}

  private record TitleRes(String title) {}

  /** 사용자 메모리 문자열 생성 */
  private String buildMemoryBlock(Long userId) {
    var rows =
        memoryRepo.findByUserIdOrderByModifiedAtDesc(userId, PageRequest.of(0, memoryMaxRows));
    if (rows.isEmpty()) {
      return "";
    }
    return rows.stream().map(m -> m.getK() + ": " + m.getV()).collect(Collectors.joining("\n"));
  }

  /** 최근 대화 턴 생성 */
  private List<Turn> buildTurns(Long conversationId) {
    var recentDesc =
        messageRepo.findByConversationIdOrderByCreatedAtDesc(
            conversationId, PageRequest.of(0, historyMaxTurns));
    var recentAsc = new ArrayList<>(recentDesc);
    recentAsc.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    return recentAsc.stream()
        .map(m -> new Turn(m.getRole().name().toLowerCase(), m.getContent()))
        .toList();
  }

  // NEW: LangChain에 제목 생성 요청
  private String requestSmartTitle(String firstMessage, Long userId) {
    try {
      var memory = buildMemoryBlock(userId);
      var res =
          langchainWebClient
              .post()
              .uri("/title")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(new TitleReq(firstMessage, memory, 20))
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  rsp ->
                      rsp.bodyToMono(String.class)
                          .map(
                              body -> new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED)))
              .bodyToMono(TitleRes.class)
              .timeout(Duration.ofMillis(timeoutMs))
              .block();

      if (res != null && res.title() != null && !res.title().isBlank()) {
        return res.title().trim();
      }
    } catch (Exception ignore) {
      // 제목 생성 실패는 치명적 아님
    }
    return null;
  }

  public ChatResponse chat(ChatRequest req) {
    if (req == null || req.userId() == null || req.message() == null || req.message().isBlank()) {
      throw new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED);
    }

    // 1) 대화방 확보
    boolean isNew = (req.conversationId() == null);
    Conversation conv =
        isNew
            ? conversationRepo.save(Conversation.builder().userId(req.userId()).title("대화").build())
            : conversationRepo
                .findByIdAndUserId(req.conversationId(), req.userId())
                .orElseThrow(() -> new CustomException(ChatbotErrorCode.CONVERSATION_NOT_FOUND));

    // 2) 유저 발화 저장
    messageRepo.save(
        Message.builder()
            .conversationId(conv.getId())
            .role(MessageRole.USER)
            .content(req.message())
            .build());

    // NEW: 새 대화면 제목 생성 시도 (실패해도 무시)
    if (isNew) {
      String smart = requestSmartTitle(req.message(), req.userId());
      if (smart != null && !smart.isBlank()) {
        conv.setTitle(smart);
        conversationRepo.save(conv);
      }
    }

    // 3) 메모리 & 히스토리
    String memory = buildMemoryBlock(req.userId());
    List<Turn> turns = buildTurns(conv.getId());

    // 4) 파이썬(LangChain) 호출
    ChatRes res;
    try {
      res =
          langchainWebClient
              .post()
              .uri("/chat/history")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(new HistoryReq(turns, memory))
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  rsp ->
                      rsp.bodyToMono(String.class)
                          .map(
                              body -> new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED)))
              .bodyToMono(ChatRes.class)
              .timeout(Duration.ofMillis(timeoutMs))
              .block();
    } catch (WebClientResponseException e) {
      throw new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED);
    } catch (Exception e) {
      throw new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED);
    }

    if (res == null || res.reply() == null) {
      throw new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED);
    }

    // 5) 어시스턴트 응답 저장
    messageRepo.save(
        Message.builder()
            .conversationId(conv.getId())
            .role(MessageRole.ASSISTANT)
            .content(res.reply())
            .build());

    return new ChatResponse(conv.getId(), res.reply());
  }

  public ConversationDetailResponse getConversationDetail(Long userId, Long conversationId) {
    var conv =
        conversationRepo
            .findById(conversationId)
            .orElseThrow(() -> new CustomException(ChatbotErrorCode.CONVERSATION_NOT_FOUND));

    // 남의 대화면 같은 에러로 응답(정보 노출 최소화)
    if (conv.getUserId() == null || !conv.getUserId().equals(userId)) {
      throw new CustomException(ChatbotErrorCode.CONVERSATION_NOT_FOUND);
    }

    var msgs = messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId);
    var items =
        msgs.stream()
            .map(m -> new MessageItem(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt()))
            .toList();

    return new ConversationDetailResponse(conv.getId(), conv.getUserId(), conv.getTitle(), items);
  }
}
