package com.likelion.picklbe.domain.favorite.dto;

import java.time.LocalDateTime;

public record IngredientCardDto(
    Long ingredientId, String name, String thumbnailUrl, String shortDesc, LocalDateTime likedAt) {}
