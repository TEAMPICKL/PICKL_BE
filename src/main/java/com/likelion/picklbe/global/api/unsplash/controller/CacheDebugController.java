package com.likelion.picklbe.global.api.unsplash.controller;

import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;
import com.likelion.picklbe.global.api.unsplash.client.UnsplashClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/debug/cache")
@RequiredArgsConstructor
public class CacheDebugController {

  private final UnsplashClient unsplashClient;
  private final KamisPriceClient kamisPriceClient;
  private final CacheManager cacheManager;

  @GetMapping("/unsplash")
  public Map<String, Object> testUnsplash(@RequestParam String q) {
    long t0 = System.currentTimeMillis();
    String url = unsplashClient.searchProduceImageUrl(q);
    long took = System.currentTimeMillis() - t0;
    return Map.of("query", q, "url", url, "tookMs", took);
  }

  @GetMapping("/kamis")
  public Map<String, Object> testKamis() {
    long t0 = System.currentTimeMillis();
    var dto = kamisPriceClient.fetchPriceData();
    long took = System.currentTimeMillis() - t0;
    int count = dto != null && dto.getPrice() != null ? dto.getPrice().size() : 0;
    return Map.of("items", count, "tookMs", took);
  }

  @GetMapping("/stats")
  public Map<String, Object> stats() {
    var unsplash =
        (org.springframework.cache.caffeine.CaffeineCache) cacheManager.getCache("unsplash");
    var kamis =
        (org.springframework.cache.caffeine.CaffeineCache) cacheManager.getCache("kamisDaily");
    var sU = unsplash != null ? unsplash.getNativeCache().stats() : null;
    var sK = kamis != null ? kamis.getNativeCache().stats() : null;
    return Map.of(
        "unsplash",
            sU == null
                ? Map.of()
                : Map.of(
                    "hit",
                    sU.hitCount(),
                    "miss",
                    sU.missCount(),
                    "hitRate",
                    sU.hitRate(),
                    "eviction",
                    sU.evictionCount()),
        "kamisDaily",
            sK == null
                ? Map.of()
                : Map.of(
                    "hit",
                    sK.hitCount(),
                    "miss",
                    sK.missCount(),
                    "hitRate",
                    sK.hitRate(),
                    "eviction",
                    sK.evictionCount()));
  }

  @PostMapping("/evict")
  public String evictAll() {
    if (cacheManager.getCache("unsplash") != null) {
      cacheManager.getCache("unsplash").clear();
    }
    if (cacheManager.getCache("kamisDaily") != null) {
      cacheManager.getCache("kamisDaily").clear();
    }
    return "cleared";
  }
}
