package com.likelion.picklbe.domain.seasonitems.recipe.dto.response;

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
public class RecipeDto {

  @Schema(description = "레시피 ID", example = "1")
  private Long id;

  @Schema(description = "레시피 이름", example = "옥수수 전")
  private String recipeName;

  @Schema(description = "레시피 준비물", example = "옥수수, 밀가루, 소금")
  private String ingredients;

  @Schema(description = "레시피 조리 방법", example = "옥수수를 갈고 밀가루와 섞어 부쳐주세요")
  private String instructions;

  @Schema(description = "레시피 꿀팁", example = "기름을 두르고 약불에 천천히 익히세요")
  private String tip;

  @Schema(description = "조리 시간(문자열)", example = "약 7~10분")
  private String cookingTime;

  @Schema(description = "추천 분류(배열)", example = "[\"간식\",\"아침\"]")
  private List<String> recommendTags; // 배열
}
