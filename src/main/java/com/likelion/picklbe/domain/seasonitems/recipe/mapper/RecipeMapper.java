package com.likelion.picklbe.domain.seasonitems.recipe.mapper;

import java.util.Arrays;
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
        .cookingTime(recipe.getCookingTimeText())
        .recommendTags(splitCsv(recipe.getRecommendTagsCsv()))
        .build();
  }

  public List<RecipeDto> toDtoList(List<Recipe> recipes) {
    return recipes.stream().map(this::toDto).collect(Collectors.toList());
  }

  // === CSV <-> List 유틸 ===
  public static List<String> splitCsv(String csv) {
    if (csv == null || csv.isBlank()) {
      return List.of();
    }
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  public static String joinCsv(List<String> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    return list.stream()
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining(","));
  }
}
