package com.likelion.picklbe.domain.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizAnswerResult {

  @Schema(description = "정답 여부(CORRECT/WRONG)")
  private String result;

  @Schema(description = "이번 응답으로 적립된 포인트(정답일 때만 > 0)")
  private Integer awarded;

  @Schema(description = "현재 지갑 잔액 (nullable)")
  private Long walletBalance;

  @Schema(description = "다음 행동 유형 (PRICE_CURRENT | PRICE_TODAY)")
  private String cta;

  @Schema(description = "연결할 재료 ID (가격 화면 이동에 사용)")
  private Long ingredientId;

  @Schema(description = "프론트가 바로 이동할 라우트/딥링크 경로 (예: /price/2?view=current)")
  private String actionPath;

  @Schema(description = "버튼에 쓸 라벨(예: 현재가 보기 / 오늘의 가격 보러가기)")
  private String actionLabel;
}
