package com.likelion.picklbe.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizAnswerResult {

  private String result; // CORRECT | WRONG
  private Integer awarded;
  private Long walletBalance; // nullable
  private String cta; // "PRICE_DETAIL"
  private Long ingredientId;
}
