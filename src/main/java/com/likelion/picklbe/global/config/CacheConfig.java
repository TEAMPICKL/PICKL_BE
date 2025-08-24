// src/main/java/com/likelion/picklbe/global/config/CacheConfig.java
package com.likelion.picklbe.global.config;

import java.time.Duration;
import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager manager = new SimpleCacheManager();

    // KAMIS 데이터 캐시 (1일)
    CaffeineCache kamisCache =
        new CaffeineCache(
            "kamisDaily",
            Caffeine.newBuilder()
                .recordStats() // 통계 수집 ON (Actuator/metrics에서 조회 가능)
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofDays(1))
                .build());

    // Unsplash 이미지 캐시 (7일)
    CaffeineCache unsplashCache =
        new CaffeineCache(
            "unsplash",
            Caffeine.newBuilder()
                .recordStats() // 통계 수집 ON
                .maximumSize(5_000)
                .expireAfterWrite(Duration.ofDays(7))
                .build());

    manager.setCaches(List.of(kamisCache, unsplashCache));
    return manager;
  }
}
