package com.likelion.picklbe.domain.chatbot.service;

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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatbotService {

  private final ConversationRepository conversationRepo;
  private final MessageRepository messageRepo;
  private final UserMemoryRepository memoryRepo;
  private final WebClient langchainWebClient;

  private final java.util.Set<Long> inflightConversations =
      java.util.concurrent.ConcurrentHashMap.newKeySet();
  private final java.util.Set<Long> inflightNewConvByUser =
      java.util.concurrent.ConcurrentHashMap.newKeySet();

  @Value("${chatbot.history-max-turns:20}")
  private int historyMaxTurns;

  @Value("${chatbot.memory-max-rows:50}")
  private int memoryMaxRows;

  @Value("${chatbot.timeout-ms:10000}")
  private long timeoutMs;

  // ----- 공통 유틸 -----
  private String buildMemoryBlock(Long userId) {
    var rows =
        memoryRepo.findByUserIdOrderByModifiedAtDesc(userId, PageRequest.of(0, memoryMaxRows));
    if (rows.isEmpty()) {
      return "";
    }
    return rows.stream().map(m -> m.getK() + ": " + m.getV()).collect(Collectors.joining("\n"));
  }

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

  // (선택) 새 대화 제목 생성 – 실패해도 무시
  private record TitleReq(String message, String memory, int max_len) {

  }

  private record TitleRes(String title) {

  }

  private void tryUpdateSmartTitleAsync(String firstMessage, Long userId, Conversation conv) {
    var memory = buildMemoryBlock(userId);
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
                    .map(body -> new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED)))
        .bodyToMono(TitleRes.class)
        .timeout(Duration.ofMillis(timeoutMs))
        .onErrorResume(e -> Mono.empty())
        .subscribe(
            res -> {
              if (res != null && res.title() != null && !res.title().isBlank()) {
                conv.setTitle(res.title().trim());
                conversationRepo.save(conv);
              }
            });
  }

  // ----- 완성본 JSON 응답 -----
  public ChatResponse chat(ChatRequest req) {
    if (req == null || req.userId() == null || req.message() == null || req.message().isBlank()) {
      throw new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED);
    }

    boolean isNew = (req.conversationId() == null);
    Conversation conv =
        isNew
            ? conversationRepo.save(Conversation.builder().userId(req.userId()).title("대화").build())
            : conversationRepo
                .findByIdAndUserId(req.conversationId(), req.userId())
                .orElseThrow(() -> new CustomException(ChatbotErrorCode.CONVERSATION_NOT_FOUND));

    // 유저 발화 저장
    messageRepo.save(
        Message.builder()
            .conversationId(conv.getId())
            .role(MessageRole.USER)
            .content(req.message())
            .build());

    if (isNew) {
      tryUpdateSmartTitleAsync(req.message(), req.userId(), conv);
    }

    String memory = buildMemoryBlock(req.userId());
    List<Turn> turns = buildTurns(conv.getId());

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

    // AI 답변 저장
    messageRepo.save(
        Message.builder()
            .conversationId(conv.getId())
            .role(MessageRole.ASSISTANT)
            .content(res.reply())
            .build());

    return new ChatResponse(conv.getId(), res.reply());
  }

  // ----- 스트리밍 (SSE) -----
  public Flux<ServerSentEvent<String>> chatStream(ChatRequest req) {
    if (req == null || req.userId() == null || req.message() == null || req.message().isBlank()) {
      return Flux.error(new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED));
    }

    final boolean isNewRequest = (req.conversationId() == null);

    // ① 새 대화 동시 생성 가드 (userId당 1개)
    if (isNewRequest) {
      boolean ok = inflightNewConvByUser.add(req.userId());
      if (!ok) {
        return Flux.just(
            ServerSentEvent.<String>builder("already-streaming").event("busy").build());
      }
    }

    // 대화 로드/생성
    Conversation conv;
    try {
      conv =
          isNewRequest
              ? conversationRepo.save(
              Conversation.builder().userId(req.userId()).title("대화").build())
              : conversationRepo
                  .findByIdAndUserId(req.conversationId(), req.userId())
                  .orElseThrow(() -> new CustomException(ChatbotErrorCode.CONVERSATION_NOT_FOUND));
    } catch (RuntimeException e) {
      if (isNewRequest) {
        inflightNewConvByUser.remove(req.userId());
      }
      throw e;
    }

    // ② 같은 conversationId 동시 스트림 가드
    if (!inflightConversations.add(conv.getId())) {
      if (isNewRequest) {
        inflightNewConvByUser.remove(req.userId());
      }
      return Flux.just(ServerSentEvent.<String>builder("already-streaming").event("busy").build());
    }

    // 사용자 발화 저장
    messageRepo.save(
        Message.builder()
            .conversationId(conv.getId())
            .role(MessageRole.USER)
            .content(req.message())
            .build());

    if (isNewRequest) {
      tryUpdateSmartTitleAsync(req.message(), req.userId(), conv);
    }

    // 히스토리 + 메모리
    List<Turn> turns = buildTurns(conv.getId());
    var payload = new HistoryReq(turns, buildMemoryBlock(req.userId()));

    // 첫 이벤트: conversationId
    Flux<ServerSentEvent<String>> first =
        Flux.just(
            ServerSentEvent.<String>builder(String.valueOf(conv.getId()))
                .event("conversationId")
                .build());

    // ---- 업스트림: 원문 청크(String) → 줄 분리 → data:만 추출 ----
    final StringBuilder carry = new StringBuilder(); // 청크 경계에서 끊긴 줄 보관

    Flux<String> upstream =
        langchainWebClient
            .post()
            .uri("/chat/history/stream")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .header(HttpHeaders.ACCEPT_ENCODING, "identity")
            .bodyValue(payload)
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                rsp -> rsp.bodyToMono(String.class).flatMap(body -> Mono.error(
                    new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED))))
            .bodyToFlux(String.class) // <- String 청크
            .flatMap(chunk -> {
              // 청크를 '\n' 기준 라인으로 안전하게 분할
              List<String> out = new ArrayList<>();
              carry.append(chunk);
              int idx;
              while ((idx = indexOfNewline(carry)) >= 0) {
                String line = carry.substring(0, idx);
                if (!line.isEmpty() && line.charAt(line.length() - 1) == '\r') {
                  line = line.substring(0, line.length() - 1);
                }
                out.add(line);
                // '\n' 소비
                carry.delete(0, idx + 1);
              }
              return Flux.fromIterable(out);
            })
            .concatWith(Flux.defer(() -> {
              // 스트림 종료 시 남은 마지막 라인 방출
              if (carry.length() > 0) {
                String last = carry.toString();
                carry.setLength(0);
                return Flux.just(last);
              }
              return Flux.empty();
            }))
            // data: 라인만 추출
            .filter(line -> line.startsWith("data:"))
            .map(line -> line.substring(5).trim())
            .filter(s -> !s.isBlank())
            .timeout(Duration.ofMinutes(5))
            .onErrorResume(
                t ->
                    (t instanceof reactor.netty.http.client.PrematureCloseException
                        || t instanceof java.util.concurrent.TimeoutException
                        || (
                        t instanceof org.springframework.web.reactive.function.client.WebClientResponseException w
                            && w.getCause() instanceof reactor.netty.http.client.PrematureCloseException))
                        ? Flux.empty()
                        : Flux.error(t));

    StringBuilder acc = new StringBuilder();

    Flux<ServerSentEvent<String>> body =
        upstream
            .doOnNext(acc::append) // 토큰 누적
            .doFinally(
                sig -> {
                  // ✅ 마지막에 답변 저장 + 가드 해제 (성공/에러/취소 모두)
                  try {
                    String full = acc.toString();
                    if (!full.isBlank()) {
                      messageRepo.save(
                          Message.builder()
                              .conversationId(conv.getId())
                              .role(MessageRole.ASSISTANT)
                              .content(full)
                              .build());
                    }
                  } finally {
                    inflightConversations.remove(conv.getId());
                    if (isNewRequest) {
                      inflightNewConvByUser.remove(req.userId());
                    }
                  }
                })
            .map(tok -> ServerSentEvent.<String>builder(tok).build())
            .onErrorResume(
                e ->
                    Flux.just(
                        ServerSentEvent.<String>builder("오류가 발생했어요. 잠시 후 다시 시도해주세요.")
                            .event("error")
                            .build()));

    return Flux.concat(first, body);
  }

  // 줄바꿈 인덱스 찾기(\n), 못 찾으면 -1
  private static int indexOfNewline(StringBuilder sb) {
    for (int i = 0; i < sb.length(); i++) {
      if (sb.charAt(i) == '\n') {
        return i;
      }
    }
    return -1;
  }

  // ----- 상세 조회 -----
  public ConversationDetailResponse getConversationDetail(Long userId, Long conversationId) {
    var conv =
        conversationRepo
            .findById(conversationId)
            .orElseThrow(() -> new CustomException(ChatbotErrorCode.CONVERSATION_NOT_FOUND));

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