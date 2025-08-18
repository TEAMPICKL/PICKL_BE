package com.likelion.picklbe.global.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

@Configuration
public class ExternalCallGuardConfig {

  // ⬇️ 초당 1건, 대기 2초
  @Bean
  public RateLimiter kakaoRateLimiter() {
    return RateLimiter.of(
        "kakao-local",
        RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .limitForPeriod(1) // ← 1 rps
            .timeoutDuration(Duration.ofSeconds(2))
            .build());
  }

  // ⬇️ 동시 외부 호출 1개, 대기 최대 2초
  @Bean
  public Bulkhead kakaoBulkhead() {
    return Bulkhead.of(
        "kakao-local-bulkhead",
        BulkheadConfig.custom()
            .maxConcurrentCalls(1) // ← 동시성 1
            .maxWaitDuration(Duration.ofSeconds(2))
            .build());
  }

  // ⬆️ 캐시 TTL 30초 (동일 rect/page/size 재호출 줄이기)
  @Bean
  public Cache<String, Object> kakaoRectCache() {
    return Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(10_000).build();
  }
}
