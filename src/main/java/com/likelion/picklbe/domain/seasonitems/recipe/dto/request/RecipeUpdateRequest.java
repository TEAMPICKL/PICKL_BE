package com.likelion.picklbe.domain.seasonitems.recipe.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeUpdateRequest {

  @Schema(description = "레시피 이름", example = "옥수수 튀김")
  private String recipeName; // null 이면 변경 없음

  @Schema(description = "레시피 준비물", example = "옥수수, 튀김가루, 소금")
  private String ingredients; // null 이면 변경 없음

  @Schema(description = "레시피 조리 방법", example = "옥수수를 반죽 후 기름에 튀겨주세요")
  private String instructions; // null 이면 변경 없음

  @Schema(description = "레시피 꿀팁", example = "온도는 170도 유지")
  private String tip; // null 이면 변경 없음

  @Schema(description = "조리 시간(문자열)", example = "약 7~10분")
  private String cookingTime; // null 이면 변경 없음

  @Schema(description = "추천 분류(배열)", example = "[\"간식\",\"아침\"]")
  private List<String> recommendTags; // null 이면 변경 없음
}
