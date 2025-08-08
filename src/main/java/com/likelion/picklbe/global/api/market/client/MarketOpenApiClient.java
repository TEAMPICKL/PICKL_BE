package com.likelion.picklbe.global.api.market.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MarketOpenApiClient {

  private final WebClient vworldWebClient;

  @Value("${vworld.key}")
  private String apiKey;

  @Value("${vworld.domain}")
  private String domain;

  @Value("${vworld.data-layer}")
  private String dataLayer;

  public String getMarketsByBbox(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {

    MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
    q.add("service", "data");
    q.add("request", "GetFeature");
    q.add("data", dataLayer);
    q.add("key", apiKey);
    q.add("domain", domain);
    q.add("format", "JSON");
    q.add("crs", "EPSG:4326"); // Kakao와 동일
    q.add("geomFilter", String.format("BOX(%f,%f,%f,%f)", minX, minY, maxX, maxY));
    q.add("size", String.valueOf(size != null ? size : 500));
    if (page != null) q.add("page", String.valueOf(page));

    // global 레이어에서는 일단 원본 JSON String으로 반환 (유연성↑)
    return vworldWebClient
        .get()
        .uri(uri -> uri.queryParams(q).build())
        .retrieve()
        .bodyToMono(String.class)
        .onErrorResume(e -> Mono.error(new RuntimeException("V-World 호출 실패: " + e.getMessage(), e)))
        .block();
  }
}
