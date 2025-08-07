package com.likelion.picklbe.domain.seasonitems.recipe.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.seasonitems.recipe.dto.response.RecipeDto;
import com.likelion.picklbe.domain.seasonitems.recipe.entity.Recipe;

@Component
public class RecipeMapper {

  public RecipeDto toDto(Recipe recipe) {
    return RecipeDto.builder()
        .id(recipe.getId())
        .recipeName(recipe.getRecipeName())
        .ingredients(recipe.getIngredients())
        .instructions(recipe.getInstructions())
        .tip(recipe.getTip())
        .build();
  }

  public List<RecipeDto> toDtoList(List<Recipe> recipes) {
    return recipes.stream().map(this::toDto).collect(Collectors.toList());
  }
}
