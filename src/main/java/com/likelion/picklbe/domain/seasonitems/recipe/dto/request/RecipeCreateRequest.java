package com.likelion.picklbe.domain.seasonitems.recipe.dto.request;

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
}
