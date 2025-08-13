package com.likelion.picklbe.domain.user.dto.response;

import lombok.Builder;

@Builder
public record UserSummaryResponse(
    String nickname,
    String region,
    long points,
    long daysSinceFriend,
    int favoriteIngredientCount,
    int favoriteRecipeCount,
    int pickleHistoryCount) {}
