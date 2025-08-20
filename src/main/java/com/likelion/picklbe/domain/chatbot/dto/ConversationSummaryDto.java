package com.likelion.picklbe.domain.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "대화 요약 DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSummaryDto {

  @Schema(description = "대화 ID", example = "1")
  private Long id;

  @Schema(description = "대화 제목", example = "제철 음식 추천")
  private String title;
}
