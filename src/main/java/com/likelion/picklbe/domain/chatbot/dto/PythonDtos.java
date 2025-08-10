package com.likelion.picklbe.domain.chatbot.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class PythonDtos {

  @Schema(name = "Turn", description = "파이썬 서버에 전달하는 히스토리 턴")
  public record Turn(
      @Schema(description = "역할 (user/assistant/system)", example = "user") String role,
      @Schema(description = "메시지 내용", example = "안녕!") String content) {}

  @Schema(name = "ChatReq", description = "파이썬 서버 단발 요청")
  public record ChatReq(
      @Schema(description = "사용자 메시지", example = "대한민국 수도는?") String message,
      @Schema(description = "사용자 메모리(키:값 줄바꿈)", example = "favorite_food: 김치찌개\nage: 24")
          String memory) {}

  @Schema(name = "ChatRes", description = "파이썬 서버 응답")
  public record ChatRes(
      @Schema(description = "어시스턴트 응답", example = "대한민국의 수도는 서울입니다.") String reply) {}

  @Schema(name = "HistoryReq", description = "파이썬 서버 히스토리 요청")
  public record HistoryReq(
      @Schema(description = "최근 대화 턴 목록") List<Turn> messages,
      @Schema(description = "사용자 메모리(키:값 줄바꿈)", example = "favorite_food: 김치찌개\nage: 24")
          String memory) {}
}
