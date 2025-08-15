package com.likelion.picklbe.domain.favorite.dto.request;

import com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType;

public record FavoriteToggleRequest(FavoriteType type, Long targetId) {}
