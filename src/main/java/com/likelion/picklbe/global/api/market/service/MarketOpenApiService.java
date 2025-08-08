package com.likelion.picklbe.global.api.market.service;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.global.api.market.client.MarketOpenApiClient;
import com.likelion.picklbe.global.api.market.dto.VWorldResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketOpenApiService {
  private final MarketOpenApiClient client;

  public VWorldResponse findByBbox(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {
    return client.getMarketsByBbox(minX, minY, maxX, maxY, page, size);
  }
}
