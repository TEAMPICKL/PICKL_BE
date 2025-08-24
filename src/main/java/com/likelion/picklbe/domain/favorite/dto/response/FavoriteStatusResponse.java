package com.likelion.picklbe.domain.favorite.dto.response;

import java.time.LocalDateTime;

public record FavoriteStatusResponse(boolean isLiked, LocalDateTime likedAt) {}
