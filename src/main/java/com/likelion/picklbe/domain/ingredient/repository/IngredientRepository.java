package com.likelion.picklbe.domain.ingredient.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.ingredient.entity.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

  Optional<Ingredient> findByName(String name);
}
