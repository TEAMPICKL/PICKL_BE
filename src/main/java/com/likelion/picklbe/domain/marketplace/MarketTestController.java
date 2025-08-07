package com.likelion.picklbe.domain.marketplace;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.likelion.picklbe.global.api.market.client.MarketOpenApiClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/markets")
public class MarketTestController {

  private final MarketOpenApiClient marketOpenApiClient;

  @GetMapping
  public ResponseEntity<String> getMarkets() {
    String json = marketOpenApiClient.fetchMarkets();
    return ResponseEntity.ok(json);
  }
}
