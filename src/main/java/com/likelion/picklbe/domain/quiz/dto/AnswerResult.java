package com.likelion.picklbe.domain.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerResult {

  private String result; // "CORRECT" | "WRONG"
  private Integer awarded; // 적립 포인트
  private Long walletBalance; // 적립 후 잔액 (null 가능)
  private Long ingredientId;
}
