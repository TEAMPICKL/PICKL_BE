package com.likelion.picklbe.domain.chatbot.controller;

import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatRequest;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatResponse;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ConversationDetailResponse;
import com.likelion.picklbe.domain.chatbot.dto.ConversationSummaryDto;
import com.likelion.picklbe.domain.chatbot.service.ChatbotService;
import com.likelion.picklbe.global.response.BaseResponse;
import com.likelion.picklbe.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
              **요청 방법**
              - 채팅 시작: conversationId=null로 호출
              - 같은 대화 유지: 동일 conversationId로 계속 호출
              
              **응답 형식**
              - text/event-stream (SSE)
              - 매 토큰 단위로 ServerSentEvent<String> 방출
              """)
  @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<String>> chatStream(@RequestBody @Valid ChatRequest req) {
    return service.chatStream(req);
  }

  @Operation(
      summary = "채팅 내역 조회",
      description = "대화 ID로 메시지 목록(시간 오름차순)을 반환합니다. 로그인된 사용자 본인의 대화만 조회됩니다.")
  @GetMapping("/conversations/{conversationId}")
  public ResponseEntity<BaseResponse<ConversationDetailResponse>> getConversation(
      @PathVariable Long conversationId,
      @AuthenticationPrincipal CustomUserDetails me // ⬅️ 토큰에서 사용자 추출
  ) {
    var res = service.getConversationDetail(me.getId(), conversationId);
    return ResponseEntity.ok(BaseResponse.success("ok", res));
  }

  @Operation(summary = "대화 삭제", description = "conversationId로 해당 대화를 삭제합니다. (본인 대화만 가능)")
  @DeleteMapping("/conversations/{conversationId}")
  public ResponseEntity<BaseResponse<Void>> deleteConversation(
      @PathVariable Long conversationId, @AuthenticationPrincipal CustomUserDetails me) {
    service.deleteConversation(me.getId(), conversationId);
    return ResponseEntity.ok(BaseResponse.success("대화가 삭제되었습니다.", null));
    // 필요하면 204 No Content로:
    // return ResponseEntity.noContent().build();
  }

  @Operation(summary = "대화 목록(제목+ID)", description = "로그인 사용자의 대화 목록을 최신순으로 반환합니다.")
  @GetMapping("/conversations")
  public ResponseEntity<BaseResponse<List<ConversationSummaryDto>>> listConversations(
      @AuthenticationPrincipal CustomUserDetails me) {
    var list = service.listConversationSummaries(me.getId());
    return ResponseEntity.ok(BaseResponse.success("대화 목록 조회 성공", list));
  }
}
