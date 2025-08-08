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
        res.getResponse() == null || res.getResponse().getResult() == null
            ? null
            : res.getResponse().getResult().getFeatureCollection();

    if (fc == null || fc.getFeatures() == null) return List.of();

    return fc.getFeatures().stream()
        .map(
            f -> {
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
              String addr = first(str(p, "addr"), str(p, "roadAddr"), str(p, "jibunAddr"));
              Boolean parking = bool(p, "parking");

              return MarketMarkerResponse.builder()
                  .id(first(f.getId(), str(p, "id"), str(p, "emdCd")))
                  .name(name)
                  .category(category)
                  .address(addr)
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
    return v == null ? null : String.valueOf(v);
  }

  private static Boolean bool(Map<String, Object> p, String k) {
    String s = str(p, k);
    if (s == null) return null;
    if ("Y".equalsIgnoreCase(s)) return true;
    if ("N".equalsIgnoreCase(s)) return false;
    try {
      return Integer.parseInt(s) != 0;
    } catch (Exception ignored) {
    }
    return Boolean.parseBoolean(s);
  }

  private static String first(String... arr) {
    for (String s : arr) if (s != null && !s.isBlank()) return s;
    return null;
  }
}
