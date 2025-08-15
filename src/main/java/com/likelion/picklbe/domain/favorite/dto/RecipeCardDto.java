package com.likelion.picklbe.domain.favorite.dto;

import java.time.LocalDateTime;

public record RecipeCardDto(
    Long recipeId, String title, String thumbnailUrl, LocalDateTime likedAt) {}
