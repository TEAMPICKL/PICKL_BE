package com.likelion.picklbe.domain.quiz.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizDailyResponse {

  private String date; // yyyy-MM-dd
  private IngredientDto ingredient;
  private String statement;
  private List<String> options; // ["O","X"]
  private boolean attempted;

  @Getter
  @Builder
  public static class IngredientDto {

    private Long id;
    private String name;
    private String icon;
  }
}
