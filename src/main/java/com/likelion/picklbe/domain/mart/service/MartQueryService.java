package com.likelion.picklbe.domain.mart.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.brand.BrandImageResolver;
import com.likelion.picklbe.domain.mart.dto.PlaceResponse;
import com.likelion.picklbe.domain.mart.entity.Place;
import com.likelion.picklbe.domain.mart.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MartQueryService {

  private final PlaceRepository placeRepository;
  private final BrandImageResolver brandImageResolver;

  /** 브랜드명이 없으면 지점명(name)으로 키를 만들어 이미지/브랜드코드 모두 해석 */
  private BrandInfo resolveBrandInfo(Place p) {
    String key = (p.getBrand() != null && !p.getBrand().isBlank()) ? p.getBrand() : p.getName();
    String img = brandImageResolver.resolveImageUrl(key);
    String code = brandImageResolver.resolveBrandCode(key);
    return new BrandInfo(img, code);
  }

  @Transactional(readOnly = true)
  public List<PlaceResponse> findInBounds(
      double westLng,
      double southLat,
      double eastLng,
      double northLat,
      double centerLng,
      double centerLat,
      int limit) {

    return placeRepository
        .findMartsInBounds(westLng, southLat, eastLng, northLat, centerLng, centerLat, limit)
        .stream()
        .map(
            p -> {
              BrandInfo brand = resolveBrandInfo(p);
              // PlaceResponse.of(Place, imageUrl, brandCode) 시그니처 사용
              return PlaceResponse.of(p, brand.imageUrl(), brand.brandCode());
            })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PlaceResponse> findNearby(double lng, double lat, int radiusMeters, int limit) {
    return placeRepository.findMartsNearby(lng, lat, radiusMeters, limit).stream()
        .map(
            p -> {
              BrandInfo brand = resolveBrandInfo(p);
              // PlaceResponse.of(Place, imageUrl, brandCode) 시그니처 사용
              return PlaceResponse.of(p, brand.imageUrl(), brand.brandCode());
            })
        .collect(Collectors.toList());
  }

  /** 내부 전용 DTO (Java 16+): 이미지 URL과 브랜드 코드 한 번에 전달 */
  private record BrandInfo(String imageUrl, String brandCode) {}
}
