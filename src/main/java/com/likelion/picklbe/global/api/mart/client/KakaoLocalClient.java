package com.likelion.picklbe.global.api.mart.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.likelion.picklbe.global.api.mart.dto.KakaoCategoryResponse;

import com.github.benmanes.caffeine.cache.Cache;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

  private final WebClient kakaoWebClient;
  private final RateLimiter kakaoRateLimiter;
  private final Bulkhead kakaoBulkhead;
  private final Cache<String, Object> kakaoRectCache;

  // ⬇️ 같은 키의 동시 진행 중 요청을 합치는 레지스트리
  private final Map<String, Mono<KakaoCategoryResponse>> inflight = new ConcurrentHashMap<>();

  // ⬇️ 429 대응을 위한 쿨다운 시한(ms)
  private final AtomicLong cooldownUntilMs = new AtomicLong(0L);

  public KakaoCategoryResponse searchMartsByRect(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {

    final String rect = String.format("%f,%f,%f,%f", minX, minY, maxX, maxY);
    final int safePage = (page == null || page < 1) ? 1 : page;
    final int requestedSize = (size == null || size < 1) ? 5 : size;
    final int safeSize = Math.min(requestedSize, 15);

    final String key = cacheKey("MT1", rect, safePage, safeSize);

    // 0) 과대 BBOX 가드 (너무 넓은 영역은 400으로 컷)
    //   예시: 위도/경도 차의 곱이 0.30 이상이면 과대(상황에 맞게 조정)
    double area = Math.abs(maxX - minX) * Math.abs(maxY - minY);
    if (area > 0.30) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BBOX too large; zoom in more");
    }

    // 1) 캐시
    KakaoCategoryResponse cached = (KakaoCategoryResponse) kakaoRectCache.getIfPresent(key);
    if (cached != null) {
      log.debug("[KAKAO][CACHE HIT] rect={} p={} s={}", rect, safePage, safeSize);
      return cached;
    }

    // 2) 인플라이트 공유
    Mono<KakaoCategoryResponse> shared =
        inflight.computeIfAbsent(
            key,
            k -> {
              // 호출 직전: 쿨다운 체크
              long now = System.currentTimeMillis();
              long until = cooldownUntilMs.get();
              if (now < until) {
                long secs = Math.max(1, (until - now) / 1000);
                throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, "Kakao cooling down, retry after ~" + secs + "s");
              }

              log.info("[KAKAO] MT1 rect={} page={} size={}", rect, safePage, safeSize);

              Mono<KakaoCategoryResponse> pipeline =
                  kakaoWebClient
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
                      // ⬇️ 429: Retry-After 읽어서 쿨다운 갱신
                      .onStatus(
                          s -> s.value() == 429,
                          resp -> {
                            String ra =
                                resp.headers().header("Retry-After").stream()
                                    .findFirst()
                                    .orElse(null);
                            long backoffSec = 0;
                            try {
                              if (ra != null) {
                                backoffSec = Long.parseLong(ra.trim());
                              }
                            } catch (Exception ignored) {
                            }
                            if (backoffSec <= 0) {
                              backoffSec = 10; // 없으면 보수적 10초
                            }
                            long untilMs = System.currentTimeMillis() + backoffSec * 1000L;
                            cooldownUntilMs.getAndAccumulate(untilMs, Math::max); // 더 먼 미래 유지

                            log.warn(
                                "[KAKAO] 429 Too Many Requests, Retry-After={}s, cooldownUntil={}",
                                backoffSec,
                                untilMs);
                            return resp.createException()
                                .flatMap(ex -> Mono.error((Throwable) ex)); // 예외 전파
                          })
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
                      .timeout(Duration.ofSeconds(9))
                      // ⬇️ Retry-After 기반 429 재시도 (최대 3회)
                      .retryWhen(
                          Retry.max(3)
                              .filter(
                                  ex ->
                                      ex instanceof WebClientResponseException
                                          && ((WebClientResponseException) ex)
                                                  .getStatusCode()
                                                  .value()
                                              == 429)
                              .transientErrors(true)
                              .doBeforeRetry(
                                  rs ->
                                      log.warn(
                                          "[KAKAO] retrying due to 429 attempt={}/{}",
                                          rs.totalRetries() + 1,
                                          3)))
                      // ⬇️ 동시성 캡(큐잉) → QPS 리밋 → 실제 호출
                      .transformDeferred(BulkheadOperator.of(kakaoBulkhead))
                      .transformDeferred(RateLimiterOperator.of(kakaoRateLimiter))
                      .doOnSuccess(resp -> kakaoRectCache.put(key, resp))
                      .doFinally(sig -> inflight.remove(key))
                      // 여러 구독자에게 결과 공유 + 30초 캐시
                      .cache(Duration.ofSeconds(30));

              return pipeline;
            });

    KakaoCategoryResponse res = shared.block();

    if (res != null && res.getMeta() != null) {
      var m = res.getMeta();
      log.info(
          "[KAKAO] meta isEnd={} totalCount={} pageableCount={}",
          m.isEnd(),
          m.getTotalCount(),
          m.getPageableCount());
    }
    return res;
  }

  private static String cacheKey(String code, String rect, int page, int size) {
    try {
      var md = MessageDigest.getInstance("SHA-256");
      var raw = (code + "|" + rect + "|" + page + "|" + size).getBytes(StandardCharsets.UTF_8);
      return HexFormat.of().formatHex(md.digest(raw));
    } catch (Exception e) {
      return code + "|" + rect + "|" + page + "|" + size;
    }
  }
}
