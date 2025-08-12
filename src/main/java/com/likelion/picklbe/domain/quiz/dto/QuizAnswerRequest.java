package com.likelion.picklbe.domain.quiz.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerRequest {

  @NotNull private String answer; // "O" | "X"
  private String idempotencyKey; // 선택(미구현시 무시)

  public boolean asBoolean() {
    return "O".equalsIgnoreCase(answer);
  }
}
