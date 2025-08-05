package com.likelion.picklbe.domain.seasonitems.recipe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.seasonitems.recipe.entity.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
  List<Recipe> findBySeasonItemId(Long seasonItemId);
}
