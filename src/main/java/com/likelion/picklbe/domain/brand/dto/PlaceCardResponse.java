package com.likelion.picklbe.domain.brand.dto;

public record PlaceCardResponse(
    Long id,
    String name,
    String address,
    double lat,
    double lng,
    String brandCode,
    String imageUrl,
    double distanceMeters,
    int walkingMinutes) {}
