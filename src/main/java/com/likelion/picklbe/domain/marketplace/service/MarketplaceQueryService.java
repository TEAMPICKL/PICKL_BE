package com.likelion.picklbe.domain.marketplace.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.marketplace.dto.response.MarketMarkerResponse;
import com.likelion.picklbe.global.api.market.dto.VWorldResponse;
import com.likelion.picklbe.global.api.market.service.MarketOpenApiService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketplaceQueryService {

  private final MarketOpenApiService external;

  public List<MarketMarkerResponse> getMarkers(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {

    VWorldResponse res = external.findByBbox(minX, minY, maxX, maxY, page, size);

    var fc =
        (res.getResponse() == null || res.getResponse().getResult() == null)
            ? null
            : res.getResponse().getResult().getFeatureCollection();

    if (fc == null || fc.getFeatures() == null) return List.of();

    return fc.getFeatures().stream()
        .map(
            f -> {
              // 좌표 [lng, lat]
              Double lng = null, lat = null;
              if (f.getGeometry() != null
                  && f.getGeometry().getCoordinates() != null
                  && f.getGeometry().getCoordinates().size() >= 2) {
                lng = f.getGeometry().getCoordinates().get(0);
                lat = f.getGeometry().getCoordinates().get(1);
              }

              Map<String, Object> p = f.getProperties();

              String name = str(p, "name"); // 시장명
              String category = str(p, "category"); // 유형

              // 주소: 도로명 -> 지번 -> 기타 키들 순
              String address =
                  first(
                      str(p, "adr_road"),
                      str(p, "adrRoad"),
                      str(p, "roadAddr"),
                      str(p, "adr_jibun"),
                      str(p, "adrJibun"),
                      str(p, "jibunAddr"),
                      str(p, "address"),
                      str(p, "addr"));

              // 주차: Y/N/true/false/1/0 모두 허용
              Boolean parking = yn(p, "park", "parking");

              // id는 원본 id -> props.id -> market 코드 등
              String id = first(nz(f.getId()), str(p, "id"), str(p, "market"), str(p, "emdCd"));

              return MarketMarkerResponse.builder()
                  .id(id)
                  .name(name)
                  .category(category)
                  .address(address)
                  .lat(lat)
                  .lng(lng)
                  .parking(parking)
                  .build();
            })
        .toList();
  }

  private static String str(Map<String, Object> p, String k) {
    if (p == null) return null;
    Object v = p.get(k);
    if (v == null) return null;
    String s = String.valueOf(v).trim();
    return s.isEmpty() || "null".equalsIgnoreCase(s) ? null : s;
  }

  private static String nz(String s) { // null or blank -> null
    return (s == null || s.isBlank()) ? null : s;
  }

  private static String first(String... arr) {
    for (String s : arr) {
      if (s != null && !s.isBlank()) return s;
    }
    return null;
  }

  private static Boolean yn(Map<String, Object> p, String... keys) {
    for (String k : keys) {
      String s = str(p, k);
      if (s == null) continue;
      if ("Y".equalsIgnoreCase(s)) return true;
      if ("N".equalsIgnoreCase(s)) return false;
      if ("true".equalsIgnoreCase(s)) return true;
      if ("false".equalsIgnoreCase(s)) return false;
      try {
        return Integer.parseInt(s) != 0;
      } catch (Exception ignore) {
        /* not an int */
      }
    }
    return null;
  }
}
