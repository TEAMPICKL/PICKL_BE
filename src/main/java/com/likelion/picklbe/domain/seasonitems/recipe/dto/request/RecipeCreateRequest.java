package com.likelion.picklbe.domain.seasonitems.recipe.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeCreateRequest {

  @Schema(description = "레시피 이름", example = "옥수수 전")
  @NotBlank
  private String recipeName;

  @Schema(description = "레시피 준비물", example = "옥수수, 밀가루, 소금")
  @NotBlank
  private String ingredients;

  @Schema(description = "레시피 조리 방법", example = "옥수수를 갈고 밀가루와 섞어 부쳐주세요")
  @NotBlank
  private String instructions;

  @Schema(description = "레시피 꿀팁", example = "기름을 두르고 약불에 천천히 익히세요")
  @NotBlank
  private String tip;

  @Schema(description = "조리 시간(문자열)", example = "약 7~10분")
  private String cookingTime; // 문자열 그대로 받음

  @Schema(description = "추천 분류(배열)", example = "[\"간식\",\"아침\"]")
  private List<String> recommendTags; // 배열로 받되 DB에는 CSV로 저장
}
