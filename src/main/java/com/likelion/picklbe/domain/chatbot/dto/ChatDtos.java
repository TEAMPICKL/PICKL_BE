package com.likelion.picklbe.domain.chatbot.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.likelion.picklbe.domain.chatbot.entity.MessageRole;

import io.swagger.v3.oas.annotations.media.Schema;

public class ChatDtos {

  @Schema(name = "ChatRequest", description = "챗봇 대화 요청")
  public record ChatRequest(
      @Schema(description = "사용자 ID", example = "1") Long userId,
      @Schema(description = "대화 ID (첫 턴이면 null)", nullable = true, example = "null")
          Long conversationId,
      @Schema(description = "사용자 메시지", example = "대한민국 수도는?") String message) {}

  @Schema(name = "HistoryTurn", description = "대화 히스토리 한 턴")
  public record HistoryTurn(
      @Schema(description = "역할(발화자)", example = "USER", implementation = MessageRole.class)
          MessageRole role,
      @Schema(description = "메시지 내용", example = "안녕!") String content) {}

  @Schema(name = "HistoryRequest", description = "히스토리 기반 요청")
  public record HistoryRequest(
      @Schema(description = "사용자 ID", example = "1") Long userId,
      @Schema(description = "대화 ID", example = "42") Long conversationId,
      @Schema(description = "히스토리 턴 목록") List<HistoryTurn> messages) {}

  // ⬇⬇⬇ 추가: 메시지 1개를 표현하는 DTO
  @Schema(name = "MessageItem", description = "대화 메시지 1개")
  public record MessageItem(
      @Schema(description = "메시지 ID", example = "12") Long id,
      @Schema(description = "역할", example = "USER") MessageRole role,
      @Schema(description = "내용", example = "대한민국 수도는?") String content,
      @Schema(description = "작성 시각", example = "2025-08-11T03:32:10.809456")
          LocalDateTime createdAt) {}

  @Schema(name = "ChatResponse", description = "챗봇 응답")
  public record ChatResponse(
      @Schema(description = "대화 ID(새로 생성되었거나 기존)", example = "42") Long conversationId,
      @Schema(description = "어시스턴트 응답", example = "대한민국의 수도는 서울입니다.") String reply) {}

  @Schema(name = "ConversationDetailResponse", description = "대화 상세(메시지 목록 포함)")
  public record ConversationDetailResponse(
      @Schema(description = "대화 ID", example = "4") Long conversationId,
      @Schema(description = "사용자 ID", example = "1") Long userId,
      @Schema(description = "대화 제목", example = "대화") String title,
      @Schema(description = "메시지 목록") List<MessageItem> messages) {}
}
