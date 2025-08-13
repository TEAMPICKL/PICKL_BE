package com.likelion.picklbe.domain.seasonitems.recipe.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.seasonitems.recipe.dto.request.RecipeCreateRequest;
import com.likelion.picklbe.domain.seasonitems.recipe.dto.response.RecipeDto;
import com.likelion.picklbe.domain.seasonitems.recipe.service.RecipeService;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/season-items/{seasonItemId}/recipes")
@Tag(name = "Recipe", description = "메인 페이지 - 제철 식재료 레시피 API")
public class RecipeController {

  private final RecipeService recipeService;

  @GetMapping
  @Operation(
      summary = "래시피 조회",
      description =
          """
              제철 식재료의 래시피들을 반환합니다\n
              파라미터: 제철 식재료 id(id)\n
              필드: 래시피 이름(recipeName), 필요한 재료(ingredients), 요리 방법(instructions), 팁(tip)
              """)
  public BaseResponse<List<RecipeDto>> getRecipesBySeasonItemId(@PathVariable Long seasonItemId) {
    return BaseResponse.success("레시피 조회 성공", recipeService.getRecipesBySeasonItemId(seasonItemId));
  }

  @PostMapping
  @Operation(summary = "Dev 레시피 생성", description = "해당 제철 식재료(seasonItemId)에 레시피를 추가합니다.")
  public BaseResponse<RecipeDto> createRecipe(
      @PathVariable Long seasonItemId, @RequestBody @Valid RecipeCreateRequest request) {
    return BaseResponse.success("레시피 생성 성공", recipeService.create(seasonItemId, request));
  }
}
