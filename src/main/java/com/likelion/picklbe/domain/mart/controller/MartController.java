package com.likelion.picklbe.domain.mart.controller;

import com.likelion.picklbe.domain.marketplace.dto.response.MarketMarkerResponse;
import com.likelion.picklbe.domain.mart.service.MartQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MartController {

  private final MartQueryService service;

  /**
   * 현재 지도 bbox로 대형마트(MT1) 목록 조회
   */
  @GetMapping("/api/marts")
  public List<MarketMarkerResponse> getMarts(
      @RequestParam double minX, // lng west
      @RequestParam double minY, // lat south
      @RequestParam double maxX, // lng east
      @RequestParam double maxY, // lat north
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size
  ) {
    log.info("[API] /api/marts called minX={}, minY={}, maxX={}, maxY={}, page={}, size={}",
        minX, minY, maxX, maxY, page, size);
    return service.getMarts(minX, minY, maxX, maxY, page, size);
  }
}