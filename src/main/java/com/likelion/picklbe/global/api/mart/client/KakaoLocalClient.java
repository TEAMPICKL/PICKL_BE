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
   * 카카오 로컬 카테고리(대형마트 MT1) - 사각형(BBOX) 검색 - size 기본 5(최대 15로 캡) - page 기본값 1 보정 - 429(레이트리밋) 시
   * 지수백오프로 최대 4회 재시도(1s 시작) - 타임아웃 5초
   */
  public KakaoCategoryResponse searchMartsByRect(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {

    final String rect = String.format("%f,%f,%f,%f", minX, minY, maxX, maxY);
    final int safePage = (page == null || page < 1) ? 1 : page;
    final int requestedSize = (size == null || size < 1) ? 5 : size;
    final int safeSize = Math.min(requestedSize, 15); // 카카오 제한

    log.info("[KAKAO] MT1 rect={}, page={}, size={}", rect, safePage, safeSize);

    return kakaoWebClient
        .get()
        .uri(
            uri ->
                uri.path("/v2/local/search/category.json")
                    .queryParam("category_group_code", "MT1")
                    .queryParam("rect", rect)
                    .queryParam("page", safePage)
                    .queryParam("size", safeSize)
                    .build())
        .retrieve()
        // 429 전용 로깅 + 예외 전달(WebClientResponseException 유지)
        .onStatus(
            s -> s.value() == 429,
            resp ->
                resp.createException()
                    .flatMap(
                        ex -> {
                          log.warn(
                              "[KAKAO] 429 Too Many Requests rect={} page={} size={}",
                              rect,
                              safePage,
                              safeSize);
                          return Mono.error(ex);
                        }))
        // 기타 4xx/5xx 공통 처리
        .onStatus(
            HttpStatusCode::isError,
            resp ->
                resp.createException()
                    .flatMap(
                        ex -> {
                          log.error(
                              "[KAKAO] ERROR status={} rect={} page={} size={} msg={}",
                              resp.statusCode(),
                              rect,
                              safePage,
                              safeSize,
                              ex.getMessage());
                          return Mono.error(ex);
                        }))
        .bodyToMono(KakaoCategoryResponse.class)
        // 429만 지수백오프로 재시도 (최대 4회, 1s부터 시작)
        .retryWhen(
            Retry.backoff(4, Duration.ofSeconds(1))
                .jitter(0.5)
                .filter(
                    ex ->
                        ex instanceof WebClientResponseException
                            && ((WebClientResponseException) ex).getStatusCode().value() == 429))
        // 안전 타임아웃 5초
        .timeout(Duration.ofSeconds(5))
        .block();
  }
}
