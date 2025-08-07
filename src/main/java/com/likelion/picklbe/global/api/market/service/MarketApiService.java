package com.likelion.picklbe.global.api.market.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.global.api.market.client.MarketOpenApiClient;
import com.likelion.picklbe.global.api.market.dto.MarketApiResponseDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketApiService {

  private final MarketOpenApiClient apiClient;
  private final ObjectMapper objectMapper;

  public List<MarketApiResponseDto> getMarketList() {
    String json = apiClient.fetchMarkets();

    try {
      JsonNode root = objectMapper.readTree(json);
      JsonNode items = root.path("body").path("items");

      return objectMapper.readValue(
          items.toString(), new TypeReference<List<MarketApiResponseDto>>() {});
    } catch (Exception e) {
      throw new RuntimeException("파싱 실패", e);
    }
  }
}
