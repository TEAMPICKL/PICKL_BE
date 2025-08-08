package com.likelion.picklbe.domain.marketplace.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.marketplace.dto.response.MarketMarkerResponse;
import com.likelion.picklbe.domain.marketplace.service.MarketplaceQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MarketplaceController {

  private final MarketplaceQueryService service;

  /** 현재 지도 bbox로 전통시장 마커 목록 조회 */
  @GetMapping("/api/markets")
  public List<MarketMarkerResponse> getMarkets(
      @RequestParam double minX, // lng west
      @RequestParam double minY, // lat south
      @RequestParam double maxX, // lng east
      @RequestParam double maxY, // lat north
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return service.getMarkers(minX, minY, maxX, maxY, page, size);
  }
}
