package com.likelion.picklbe.domain.seasonitems.recipe.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.seasonitems.entity.SeasonItem;
import com.likelion.picklbe.domain.seasonitems.exception.SeasonItemErrorCode;
import com.likelion.picklbe.domain.seasonitems.recipe.dto.request.RecipeCreateRequest;
import com.likelion.picklbe.domain.seasonitems.recipe.dto.request.RecipeUpdateRequest;
import com.likelion.picklbe.domain.seasonitems.recipe.dto.response.RecipeDto;
import com.likelion.picklbe.domain.seasonitems.recipe.entity.Recipe;
import com.likelion.picklbe.domain.seasonitems.recipe.mapper.RecipeMapper;
import com.likelion.picklbe.domain.seasonitems.recipe.repository.RecipeRepository;
import com.likelion.picklbe.domain.seasonitems.repository.SeasonItemRepository;
import com.likelion.picklbe.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {

  private final RecipeRepository recipeRepository;
  private final RecipeMapper recipeMapper;
  private final SeasonItemRepository seasonItemRepository;

  @Transactional(readOnly = true)
  public List<RecipeDto> getRecipesBySeasonItemId(Long seasonItemId) {
    return recipeMapper.toDtoList(recipeRepository.findBySeasonItemId(seasonItemId));
  }

  @Transactional
  public RecipeDto create(Long seasonItemId, RecipeCreateRequest req) {
    SeasonItem seasonItem =
        seasonItemRepository
            .findById(seasonItemId)
            .orElseThrow(() -> new CustomException(SeasonItemErrorCode.SEASON_ITEM_NOT_FOUND));

    Recipe entity =
        Recipe.builder()
            .recipeName(req.getRecipeName())
            .ingredients(req.getIngredients())
            .instructions(req.getInstructions())
            .tip(req.getTip())
            .cookingTimeText(req.getCookingTime()) // 문자열 그대로 저장
            .recommendTagsCsv(RecipeMapper.joinCsv(req.getRecommendTags())) // 배열 -> CSV
            .seasonItem(seasonItem)
            .build();

    return recipeMapper.toDto(recipeRepository.save(entity));
  }

  @Transactional
  public RecipeDto update(Long seasonItemId, Long recipeId, RecipeUpdateRequest req) {
    // seasonItemId 소유의 레시피만 수정
    Recipe recipe =
        recipeRepository
            .findByIdAndSeasonItemId(recipeId, seasonItemId)
            .orElseThrow(() -> new CustomException(SeasonItemErrorCode.SEASON_ITEM_NOT_FOUND));

    // 부분 업데이트: null 아닌 값만 반영
    Recipe updated =
        Recipe.builder()
            .id(recipe.getId())
            .recipeName(req.getRecipeName() != null ? req.getRecipeName() : recipe.getRecipeName())
            .ingredients(
                req.getIngredients() != null ? req.getIngredients() : recipe.getIngredients())
            .instructions(
                req.getInstructions() != null ? req.getInstructions() : recipe.getInstructions())
            .tip(req.getTip() != null ? req.getTip() : recipe.getTip())
            .cookingTimeText(
                req.getCookingTime() != null ? req.getCookingTime() : recipe.getCookingTimeText())
            .recommendTagsCsv(
                req.getRecommendTags() != null
                    ? RecipeMapper.joinCsv(req.getRecommendTags())
                    : recipe.getRecommendTagsCsv())
            .seasonItem(recipe.getSeasonItem()) // 소유 관계 유지
            .build();

    return recipeMapper.toDto(recipeRepository.save(updated));
  }

  @Transactional
  public void delete(Long seasonItemId, Long recipeId) {
    Recipe recipe =
        recipeRepository
            .findByIdAndSeasonItemId(recipeId, seasonItemId)
            .orElseThrow(() -> new CustomException(SeasonItemErrorCode.SEASON_ITEM_NOT_FOUND));
    recipeRepository.delete(recipe);
  }
}
