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

  /**
   * brand 컬럼이 있으면 우선 사용, 없으면 name 기반으로 브랜드 추정. brandCode를 한 번만 해석해서 imageUrl은 imageUrlForCode(...)로
   * 생성해 중복 파싱을 피한다.
   */
  private BrandInfo resolveBrandInfo(Place p) {
    String key = (p.getBrand() != null && !p.getBrand().isBlank()) ? p.getBrand() : p.getName();
    String code = brandImageResolver.resolveBrandCode(key); // ex) "emart", "gs-super", "default"
    String img = brandImageResolver.imageUrlForCode(code); // baseUrl/brandPath/default 처리 포함
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
              return PlaceResponse.of(p, brand.imageUrl(), brand.brandCode());
            })
        .collect(Collectors.toList());
  }

  /** 내부 전용 DTO: 이미지 URL과 브랜드 코드 동시 전달 */
  private record BrandInfo(String imageUrl, String brandCode) {}
}
