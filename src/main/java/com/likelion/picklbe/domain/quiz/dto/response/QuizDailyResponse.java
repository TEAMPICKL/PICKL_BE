package com.likelion.picklbe.domain.quiz.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QuizDailyResponse {

  private String date; // yyyy-MM-dd
  private IngredientDto ingredient;
  private String statement;
  private List<String> options; // ["O","X"]
  private boolean attempted;
  private boolean canAnswer;
  private int remainingAttempts;

  @Getter
  @Builder
  @AllArgsConstructor
  public static class IngredientDto {

    private Long id;
    private String name;
    private String iconUrl;
  }

  /** 오늘의/추가 시도용 퀴즈 응답 - IngredientDto 직접 전달 */
  public static QuizDailyResponse of(
      LocalDate date, IngredientDto ingredient, String statement, int remaining) {
    return QuizDailyResponse.builder()
        .date(date.toString())
        .ingredient(ingredient)
        .statement(statement)
        .options(List.of("O", "X"))
        .attempted(remaining == 0)
        .canAnswer(remaining > 0)
        .remainingAttempts(remaining)
        .build();
  }

  /** 오늘의/추가 시도용 퀴즈 응답 - 엔티티 바로 전달(서비스 편의용) 패키지 경로는 프로젝트 구조에 맞춰 조정하세요. */
  public static QuizDailyResponse of(
      LocalDate date,
      com.likelion.picklbe.domain.ingredient.entity.Ingredient ing,
      String statement,
      int remaining) {
    IngredientDto dto =
        (ing == null)
            ? null
            : IngredientDto.builder()
                .id(ing.getId())
                .name(ing.getName())
                .iconUrl(ing.getIcon())
                .build();

    return of(date, dto, statement, remaining);
  }
}
