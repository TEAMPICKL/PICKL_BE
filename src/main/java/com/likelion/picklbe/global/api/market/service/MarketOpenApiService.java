package com.likelion.picklbe.global.api.market.service;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.global.api.market.client.MarketOpenApiClient;
import com.likelion.picklbe.global.api.market.dto.VWorldResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketOpenApiService {

  private final MarketOpenApiClient client;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public VWorldResponse findByBbox(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {
    String json = client.getMarketsByBbox(minX, minY, maxX, maxY, page, size);
    try {
      return objectMapper.readValue(json, VWorldResponse.class);
    } catch (Exception e) {
      throw new IllegalStateException("V-World 응답 파싱 실패", e);
    }
  }
}
