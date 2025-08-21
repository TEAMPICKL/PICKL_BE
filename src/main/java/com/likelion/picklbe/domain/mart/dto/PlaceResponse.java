package com.likelion.picklbe.domain.mart.dto;

import com.likelion.picklbe.domain.mart.entity.Place;

import lombok.Builder;

@Builder
public record PlaceResponse(
    Long id,
    String name,
    String brand,
    String category,
    String address,
    double lat,
    double lng,
    String imageUrl,
    String brandCode) {

  /** 신규 시그니처: 이미지 URL + 브랜드 코드 동시 전달 */
  public static PlaceResponse of(Place p, String imageUrl, String brandCode) {
    return PlaceResponse.builder()
        .id(p.getId())
        .name(p.getName())
        .brand(p.getBrand())
        .category(p.getCategory() != null ? p.getCategory().name() : null) // null-safe
        .address(p.getAddress())
        .lat(p.getLat())
        .lng(p.getLng())
        .imageUrl(imageUrl)
        .brandCode(brandCode)
        .build();
  }

  /** 하위 호환용 시그니처: 기존 코드가 깨지지 않도록 유지 */
  public static PlaceResponse of(Place p, String imageUrl) {
    return of(p, imageUrl, null);
  }
}
