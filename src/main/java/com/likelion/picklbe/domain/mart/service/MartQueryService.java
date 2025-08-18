package com.likelion.picklbe.domain.mart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.marketplace.dto.response.MarketMarkerResponse;
import com.likelion.picklbe.global.api.mart.client.KakaoLocalClient;
import com.likelion.picklbe.global.api.mart.dto.KakaoCategoryResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MartQueryService {

  private final KakaoLocalClient kakao;

  public List<MarketMarkerResponse> getMarts(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {

    int p = (page == null || page < 1) ? 1 : page;
    int s = (size == null || size < 1 || size > 15) ? 15 : size;

    KakaoCategoryResponse resp = kakao.searchMartsByRect(minX, minY, maxX, maxY, p, s);

    if (resp == null || resp.getDocuments() == null) {
      log.warn(
          "[MART] Upstream returned null/empty documents (rect={},{}~{},{} p={} s={})",
          minX,
          minY,
          maxX,
          maxY,
          p,
          s);
      return List.of();
    }

    var meta = resp.getMeta();
    if (meta != null) {
      log.info(
          "[MART] meta: isEnd={} totalCount={} pageableCount={}",
          meta.isEnd(),
          meta.getTotalCount(),
          meta.getPageableCount());
    }

    return resp.getDocuments().stream()
        .map(
            d ->
                MarketMarkerResponse.builder()
                    .id(d.getId())
                    .name(d.getPlaceName())
                    .category("대형마트")
                    .address(first(d.getRoadAddressName(), d.getAddressName()))
                    .lng(parse(d.getX()))
                    .lat(parse(d.getY()))
                    .parking(null)
                    .build())
        .toList();
  }

  private static String first(String... arr) {
    for (String s : arr) {
      if (s != null && !s.isBlank()) {
        return s;
      }
    }
    return null;
  }

  private static Double parse(String v) {
    try {
      return v == null ? null : Double.valueOf(v);
    } catch (Exception e) {
      return null;
    }
  }
}
