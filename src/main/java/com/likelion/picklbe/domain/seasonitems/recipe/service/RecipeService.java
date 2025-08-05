package com.likelion.picklbe.domain.seasonitems.recipe.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.seasonitems.recipe.dto.response.RecipeDto;
import com.likelion.picklbe.domain.seasonitems.recipe.mapper.RecipeMapper;
import com.likelion.picklbe.domain.seasonitems.recipe.repository.RecipeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {

  private final RecipeRepository recipeRepository;
  private final RecipeMapper recipeMapper;

  public List<RecipeDto> getRecipesBySeasonItemId(Long seasonItemId) {
    return recipeMapper.toDtoList(recipeRepository.findBySeasonItemId(seasonItemId));
  }
}
