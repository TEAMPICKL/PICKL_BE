package com.likelion.picklbe.global.api.mart.client;

import java.time.Duration;
import java.util.Collections;

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
   * 카카오 로컬 카테고리(대형마트 MT1) - BBOX(rect) 검색 - size 기본 5, 최대 15로 제한 - page 기본 1 보정 - 429(레이트리밋) 시
   * 지수백오프 재시도(최대 3회, 1s 시작, maxBackoff 4s, jitter 0.5) - 각 시도 per-attempt timeout 9초 - 최종 실패 또는
   * 타임아웃 시 "빈 결과"로 폴백
   */
  public KakaoCategoryResponse searchMartsByRect(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {

    final String rect = String.format("%f,%f,%f,%f", minX, minY, maxX, maxY);
    final int safePage = (page == null || page < 1) ? 1 : page;
    final int requestedSize = (size == null || size < 1) ? 5 : size;
    final int safeSize = Math.min(requestedSize, 15); // 카카오 API 제한

    log.info("[KAKAO] MT1 rect={}, page={}, size={}", rect, safePage, safeSize);

    return kakaoWebClient
        .get()
        .uri(
            uri ->
                uri.path("/v2/local/search/category.json")
                    .queryParam("category_group_code", "MT1")
                    .queryParam("rect", rect) // rect 포맷: 좌X(경도), 좌Y(위도), 우X(경도), 우Y(위도)
                    .queryParam("page", safePage)
                    .queryParam("size", safeSize)
                    .build())
        .retrieve()
        // 429 전용 로깅 (예외는 재시도 루프로 전달)
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
        // 그 외 4xx/5xx 공통 로깅
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
        // 각 시도별 응답 타임아웃(9s) — Netty responseTimeout(10s)보다 약간 짧게
        .timeout(Duration.ofSeconds(9))
        // 429만 지수 백오프로 재시도 (최대 3회, 1s → 2s → 4s, 상한 4s, 지터 0.5)
        .retryWhen(
            Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(4))
                .jitter(0.5)
                .filter(
                    ex ->
                        ex instanceof WebClientResponseException
                            && ((WebClientResponseException) ex).getStatusCode().value() == 429))
        // ✅ 예외 체인을 따라가며 429/타임아웃 식별하여 폴백
        .onErrorResume(
            ex -> {
              Throwable t = ex;

              // 예외 체인을 끝까지 순회
              while (t != null) {
                // 429 (재시도 후에도 계속 429)
                if (t instanceof WebClientResponseException w && w.getStatusCode().value() == 429) {
                  log.warn(
                      "[KAKAO] 429 after retries -> fallback empty (rect={}, page={}, size={})",
                      rect,
                      safePage,
                      safeSize);
                  return Mono.just(emptyResponse());
                }

                // 타임아웃 유형들 (Reactor TimeoutException, Netty HttpClientResponseTimeoutException 등)
                if (t instanceof java.util.concurrent.TimeoutException
                    || t.getClass().getName().contains("HttpClientResponseTimeoutException")) {
                  log.warn(
                      "[KAKAO] timeout -> fallback empty (rect={}, page={}, size={})",
                      rect,
                      safePage,
                      safeSize);
                  return Mono.just(emptyResponse());
                }

                t = t.getCause();
              }

              // 그 외는 그대로 전파 (서버 에러로 처리)
              return Mono.error(ex);
            })
        // block()은 상위 서비스/컨트롤러에서 동기적으로 필요하기 때문에 유지
        .block();
  }

  /** 빈 결과 폴백: documents는 빈 리스트, meta는 0/종료 상태로 세팅 */
  private KakaoCategoryResponse emptyResponse() {
    KakaoCategoryResponse r = new KakaoCategoryResponse();
    r.setDocuments(Collections.emptyList());
    KakaoCategoryResponse.Meta meta = new KakaoCategoryResponse.Meta();
    meta.setTotalCount(0);
    meta.setPageableCount(0);
    meta.setEnd(true);
    r.setMeta(meta);
    return r;
  }
}
