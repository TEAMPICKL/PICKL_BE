package com.likelion.picklbe.global.api.unsplash.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UnsplashClient {

  private final WebClient webClient;

  public UnsplashClient(@Value("${unsplash.client.id}") String clientId) {
    this.webClient =
        WebClient.builder()
            .baseUrl("https://api.unsplash.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Client-ID " + clientId)
            .defaultHeader("Accept-Version", "v1")
            .build();
  }

  /** 아주 단순: '품목명/세부' 에서 '/' 앞부분만 추려서 그대로 검색 → 첫 번째 사진 URL 반환 (없으면 null) */
  public String searchFirstImageUrl(String productName) {
    String q = normalize(productName);
    try {
      JsonNode root =
          webClient
              .get()
              .uri(
                  u ->
                      u.path("/search/photos")
                          .queryParam("query", q)
                          .queryParam("per_page", 1)
                          .queryParam("orientation", "squarish")
                          .build())
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .bodyToMono(JsonNode.class)
              .timeout(Duration.ofSeconds(3))
              .block();

      if (root == null || !root.has("results") || root.get("results").isEmpty()) {
        return null;
      }
      JsonNode first = root.get("results").get(0);
      // small 우선, 없으면 regular
      String url = first.path("urls").path("small").asText(null);
      if (url == null || url.isBlank()) {
        url = first.path("urls").path("regular").asText(null);
      }
      return (url == null || url.isBlank()) ? null : url;

    } catch (Exception e) {
      log.warn("Unsplash fetch failed. query={}", q, e);
      return null;
    }
  }

  /** 기존 호환용: 내부적으로 단순 검색 메서드로 위임 */
  @Cacheable(cacheNames = "unsplash", key = "#productKo", unless = "#result == null")
  public String searchProduceImageUrl(String productKo) {
    log.info("[Unsplash MISS] query={}", productKo);
    return searchFirstImageUrl(productKo);
  }

  /** "배추/여름(고랭지)" -> "배추" 로 단순 정규화 */
  private String normalize(String name) {
    if (name == null) {
      return "vegetable";
    }
    String main = name.split("/", 2)[0];
    main =
        main.replaceAll("[()\\[\\]{}]", " ")
            .replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    return main.isEmpty() ? "vegetable" : main;
  }
}
