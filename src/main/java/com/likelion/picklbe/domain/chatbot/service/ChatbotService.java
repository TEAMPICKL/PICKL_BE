package com.likelion.picklbe.domain.chatbot.service;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatRequest;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatResponse;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ConversationDetailResponse;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.MessageItem;
import com.likelion.picklbe.domain.chatbot.dto.ConversationSummaryDto;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatbotService {

  private final ConversationRepository conversationRepo;
  private final MessageRepository messageRepo;
  private final UserMemoryRepository memoryRepo;
  private final WebClient langchainWebClient;

  private static final DateTimeFormatter KOR_DAY_FMT =
      DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN);

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
  private record TitleReq(String message, String memory, int max_len) {}

  private record TitleRes(String title) {}

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
    conversationRepo.touchModifiedAt(conv.getId());
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
    conversationRepo.touchModifiedAt(conv.getId());
    return new ChatResponse(conv.getId(), res.reply());
  }

  // ----- 스트리밍 (SSE) -----
  public Flux<ServerSentEvent<String>> chatStream(ChatRequest req) {
    if (req == null || req.userId() == null || req.message() == null || req.message().isBlank()) {
      return Flux.error(new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED));
    }

    final boolean isNewRequest = (req.conversationId() == null);

    if (isNewRequest) {
      boolean ok = inflightNewConvByUser.add(req.userId());
      if (!ok) {
        return Flux.just(
            ServerSentEvent.<String>builder("already-streaming").event("busy").build());
      }
    }

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

    if (!inflightConversations.add(conv.getId())) {
      if (isNewRequest) {
        inflightNewConvByUser.remove(req.userId());
      }
      return Flux.just(ServerSentEvent.<String>builder("already-streaming").event("busy").build());
    }

    messageRepo.save(
        Message.builder()
            .conversationId(conv.getId())
            .role(MessageRole.USER)
            .content(req.message())
            .build());
    conversationRepo.touchModifiedAt(conv.getId());
    if (isNewRequest) {
      tryUpdateSmartTitleAsync(req.message(), req.userId(), conv);
    }

    List<Turn> turns = buildTurns(conv.getId());
    var payload = new HistoryReq(turns, buildMemoryBlock(req.userId()));

    // 첫 이벤트: conversationId
    Flux<ServerSentEvent<String>> first =
        Flux.just(
            ServerSentEvent.<String>builder(String.valueOf(conv.getId()))
                .event("conversationId")
                .build());

    // ---- 업스트림: SSE 이벤트로 직접 디코드 ----
    Flux<ServerSentEvent<String>> raw =
        langchainWebClient
            .post()
            .uri("/chat/history/stream")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .header(HttpHeaders.ACCEPT_ENCODING, "identity")
            .bodyValue(payload)
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
            .timeout(Duration.ofMinutes(5))
            // 디버그 로그
            .doOnSubscribe(s -> System.out.println("[SSE-UP][subscribe] conv=" + conv.getId()))
            .doOnNext(
                evt ->
                    System.out.println(
                        "[SSE-UP][raw] event=" + evt.event() + " data=" + evt.data()))
            .doOnError(
                e ->
                    System.err.println(
                        "[SSE-UP][raw][error] " + e.getClass().getName() + ": " + e.getMessage()))
            .doOnComplete(() -> System.out.println("[SSE-UP][raw] complete"));

    // done에서 즉시 완료, data만 토큰으로 방출
    Flux<String> upstream =
        raw.takeUntil(evt -> "done".equals(evt.event())) // done 이벤트가 오면 그 시점에 완료
            .filter(evt -> evt != null && evt.data() != null) // 👈 NPE 방지: data==null 프레임 제거
            .map(ServerSentEvent::data) // 이제 null 아님
            .filter(s -> !s.isBlank()) // 공백 토큰 제거
            .doOnNext(tok -> System.out.println("[SSE-UP][token] \"" + tok + "\""))
            .onErrorResume(
                t -> {
                  System.err.println(
                      "[SSE-UP][token][error] " + t.getClass().getName() + ": " + t.getMessage());
                  if (t instanceof reactor.netty.http.client.PrematureCloseException
                      || t instanceof java.util.concurrent.TimeoutException
                      || (t
                              instanceof
                              org.springframework.web.reactive.function.client
                                  .WebClientResponseException
                              w
                          && w.getCause()
                              instanceof reactor.netty.http.client.PrematureCloseException)) {
                    return Flux.empty();
                  }
                  return Flux.error(t);
                });

    StringBuilder acc = new StringBuilder();

    Flux<ServerSentEvent<String>> body =
        upstream
            .doOnNext(acc::append) // 토큰 누적
            .doFinally(
                sig -> {
                  try {
                    String full = acc.toString();
                    System.out.println(
                        "[SSE-DOWN][finally] signal=" + sig + " len=" + full.length());
                    if (!full.isBlank()) {
                      messageRepo.save(
                          Message.builder()
                              .conversationId(conv.getId())
                              .role(MessageRole.ASSISTANT)
                              .content(full)
                              .build());
                      conversationRepo.touchModifiedAt(conv.getId());
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
                e -> {
                  System.err.println(
                      "[SSE-DOWN][error] " + e.getClass().getName() + ": " + e.getMessage());
                  return Flux.just(
                      ServerSentEvent.<String>builder("오류가 발생했어요. 잠시 후 다시 시도해주세요.")
                          .event("error")
                          .build());
                });

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
    // ⬇️ 소유권을 한 번에 확인 (찾지 못하면 404)
    var conv =
        conversationRepo
            .findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new CustomException(ChatbotErrorCode.CONVERSATION_NOT_FOUND));

    var msgs = messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId);
    var items =
        msgs.stream()
            .map(m -> new MessageItem(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt()))
            .toList();

    return new ConversationDetailResponse(conv.getId(), conv.getUserId(), conv.getTitle(), items);
  }

  /** 대화 삭제 (소유자만 가능) - 메시지 전부 삭제 후 대화 삭제 */
  @Transactional
  public void deleteConversation(Long userId, Long conversationId) {
    // 1) 소유권 검증 + 존재 확인
    var conv =
        conversationRepo
            .findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new CustomException(ChatbotErrorCode.CONVERSATION_NOT_FOUND));

    // 2) 진행중 스트리밍이면(희박하지만) 삭제 차단
    if (inflightConversations.contains(conv.getId())) {
      throw new CustomException(ChatbotErrorCode.CHATBOT_REQUEST_FAILED); // 필요시 별도 에러코드 정의
    }

    // 3) 메시지 먼저 삭제
    messageRepo.deleteByConversationId(conv.getId());

    // 4) 대화 삭제
    conversationRepo.delete(conv);
  }

  public List<ConversationSummaryDto> listConversationSummaries(Long userId) {
    return conversationRepo.findByUserIdOrderByModifiedAtDesc(userId).stream()
        .map(
            c ->
                ConversationSummaryDto.builder()
                    .id(c.getId())
                    .title(c.getTitle())
                    .createdLabel(
                        c.getCreatedAt() == null ? "" : c.getCreatedAt().format(KOR_DAY_FMT))
                    .build())
        .toList();
  }
}
