package com.likelion.picklbe.domain.chatbot.controller;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatRequest;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatResponse;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ConversationDetailResponse;
import com.likelion.picklbe.domain.chatbot.service.ChatbotService;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Tag(name = "Chatbot", description = "챗봇 대화 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
public class ChatbotController {

  private final ChatbotService service;

  @Operation(summary = "챗봇 대화(사용 x)", description = "첫 턴은 conversationId=null 로 호출합니다.")
  @PostMapping("/chat/")
  public ResponseEntity<BaseResponse<ChatResponse>> chat(@RequestBody @Valid ChatRequest req) {
    ChatResponse res = service.chat(req);
    return ResponseEntity.ok(BaseResponse.success("ok", res));
  }

  @Operation(
      summary = "챗봇 대화(스트리밍)",
      description =
          """
              **요청 방법**\n
              채팅 시작 시: conversationId=null로 호출\n
              동일 대화방 유지: 같은 conversationId로 계속 요청\n
              userId=1은 고정\n\n

              **응답 형식**\n
              text/event-stream MIME 타입\n
              매 토큰 단위가 ServerSentEvent<String> 객체로 전달됨\n
              Flux 스트림이 종료될 때까지 클라이언트는 실시간으로 텍스트를 이어받음\n

              """)
  @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<String>> chatStream(@RequestBody @Valid ChatRequest req) {
    return service.chatStream(req);
  }

  @Operation(
      summary = "대화 상세 조회",
      description = "대화 ID로 해당 대화의 메시지 목록을 시간 오름차순으로 반환합니다. userId는 본인 대화 검증용으로 필요합니다.")
  @GetMapping("/conversations/{conversationId}")
  public ResponseEntity<BaseResponse<ConversationDetailResponse>> getConversation(
      @PathVariable Long conversationId, @RequestParam Long userId) {
    var res = service.getConversationDetail(userId, conversationId);
    return ResponseEntity.ok(BaseResponse.success("ok", res));
  }
}
