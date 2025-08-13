package com.likelion.picklbe.domain.seasonitems.recipe.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.seasonitems.entity.SeasonItem;
import com.likelion.picklbe.domain.seasonitems.exception.SeasonItemErrorCode;
import com.likelion.picklbe.domain.seasonitems.recipe.dto.request.RecipeCreateRequest;
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
            .seasonItem(seasonItem)
            .build();

    return recipeMapper.toDto(recipeRepository.save(entity));
  }
}
