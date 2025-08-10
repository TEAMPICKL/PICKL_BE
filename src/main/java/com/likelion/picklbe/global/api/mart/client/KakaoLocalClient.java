package com.likelion.picklbe.global.api.mart.client;

import java.time.Duration;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.likelion.picklbe.global.api.mart.dto.KakaoCategoryResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

  private final WebClient kakaoWebClient;

  /**
   * 카카오 로컬 카테고리(대형마트 MT1) - 사각형(BBOX) 검색 - size는 카카오 제한(최대 15)로 캡핑 - page 기본값 1 보정 - 429(레이트리밋)일 때
   * 지수백오프로 3회 재시도 - 타임아웃 3초
   */
  public KakaoCategoryResponse searchMartsByRect(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {

    final String rect = String.format("%f,%f,%f,%f", minX, minY, maxX, maxY);
    final int safePage = (page == null || page < 1) ? 1 : page;
    final int safeSize = Math.min(size == null || size < 1 ? 15 : size, 15);

    log.info("[KAKAO] MT1 rect={}, page={}, size={}", rect, safePage, safeSize);

    return kakaoWebClient
        .get()
        .uri(uri -> uri
            .path("/v2/local/search/category.json")
            .queryParam("category_group_code", "MT1")
            .queryParam("rect", rect)
            .queryParam("page", safePage)
            .queryParam("size", safeSize)
            .build())
        .retrieve()
        // 429 전용 로깅 + 예외 전달(WebClientResponseException 유지)
        .onStatus(s -> s.value() == 429, resp ->
            resp.createException().flatMap(ex -> {
              log.warn("[KAKAO] 429 Too Many Requests (rect={}, page={}, size={})", rect, safePage,
                  safeSize);
              return Mono.error(ex);
            }))
        // 기타 4xx/5xx 공통 처리
        .onStatus(HttpStatusCode::isError, resp ->
            resp.createException().flatMap(ex -> {
              log.error(
                  "[KAKAO] ERROR status={} urlHint=category.json rect={} page={} size={} msg={}",
                  resp.statusCode(), rect, safePage, safeSize, ex.getMessage());
              return Mono.error(ex);
            }))
        .bodyToMono(KakaoCategoryResponse.class)
        // 429만 지수백오프로 재시도 (최대 3회)
        .retryWhen(
            Retry.backoff(3, Duration.ofMillis(300))
                .jitter(0.5)
                .filter(ex -> ex instanceof WebClientResponseException
                    && ((WebClientResponseException) ex).getStatusCode().value() == 429)
        )
        // 안전 타임아웃
        .timeout(Duration.ofSeconds(3))
        .block();
  }
}