package com.likelion.picklbe.domain.seasonitems.recipe.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.likelion.picklbe.domain.seasonitems.recipe.dto.response.RecipeDto;
import com.likelion.picklbe.domain.seasonitems.recipe.service.RecipeService;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/season-items/{seasonItemId}/recipes")
@Tag(name = "Recipe", description = "제철 식재료 레시피 API")
public class RecipeController {

  private final RecipeService recipeService;

  @GetMapping
  @Operation(summary = "제철 식재료에 맞는 추천 레시피 리스트 반환")
  public BaseResponse<List<RecipeDto>> getRecipesBySeasonItemId(@PathVariable Long seasonItemId) {
    List<RecipeDto> recipes = recipeService.getRecipesBySeasonItemId(seasonItemId);
    return BaseResponse.success("레시피 조회 성공", recipes);
  }
}
