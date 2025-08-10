package com.likelion.picklbe.global.api.mart.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.likelion.picklbe.global.api.mart.dto.KakaoCategoryResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

  private final WebClient kakaoWebClient;

  public KakaoCategoryResponse searchMartsByRect(
      double minX, double minY, double maxX, double maxY, int page, int size) {

    String rect = String.format("%f,%f,%f,%f", minX, minY, maxX, maxY);
    log.info("[KAKAO] MT1 rect={}, page={}, size={}", rect, page, size);

    return kakaoWebClient
        .get()
        .uri(
            uri ->
                uri.path("/v2/local/search/category.json") // ← 여기!
                    .queryParam("category_group_code", "MT1")
                    .queryParam("rect", rect)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .build())
        .retrieve()
        .bodyToMono(KakaoCategoryResponse.class)
        .block();
  }
}
