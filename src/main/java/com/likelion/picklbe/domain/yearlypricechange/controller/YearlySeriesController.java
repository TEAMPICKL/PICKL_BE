package com.likelion.picklbe.domain.yearlypricechange.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.yearlypricechange.dto.YearlyPriceChangeRawDto;
import com.likelion.picklbe.domain.yearlypricechange.service.YearlyPriceChangeService;
import com.likelion.picklbe.domain.yearlypricechange.service.YearlySeriesPersistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/kamis/yearly")
@Tag(name = "Yearly", description = "그래프용 5년치 가격(원시/시계열) API")
@RequiredArgsConstructor
@Validated
public class YearlySeriesController {

  private final YearlyPriceChangeService yearlyPriceChangeService; // raw 조회용
  private final YearlySeriesPersistService seriesService; // 시계열/적재용

  // -----------------------------
  // RAW (원시 데이터)
  // -----------------------------
  @GetMapping("/raw/batch")
  @Operation(summary = "DEV - 연도별 가격 KAMIS 최신 데이터 조회", description = "연도별 전체 raw 데이터 조회")
  public Map<String, YearlyPriceChangeRawDto> getRawBatch(
      @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @RequestParam(name = "market", required = false) String market) {
    log.info("[YEARLY][RAW][BATCH] request date={}, market={}", date, market);
    return yearlyPriceChangeService.getRawBatch(date, market);
  }

  // -----------------------------
  // SERIES (시계열/적재)
  // -----------------------------
  @PostMapping("/series/ingest")
  @Operation(summary = "DEV - 연도별 가격 KAMIS 최신 데이터 적재", description = "연도별 전체 raw 데이터 적재")
  public Map<String, Object> ingest(
      @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @RequestParam(name = "market", required = false) String market) {
    return seriesService.ingestAll(date, market);
  }

  @GetMapping("/series/item")
  @Operation(summary = "그래프 - 연도별 전체 식재료 가격 조회", description = "5년치 전체 식재료 가격 조회")
  public Map<String, Map<String, Double>> getAllItemSeriesOrOne(
      @RequestParam(name = "productNo", required = false) String productNo) {
    if (productNo == null || productNo.isBlank()) {
      // Map<productNo, Map<yyyy, max>>
      return seriesService.getAllItemSeries();
    }
    // 단일 품목은 Map<yyyy, max>인데, 상위 반환형을 위해 감싸서 반환
    return Map.of(productNo, seriesService.getItemSeries(productNo));
  }

  @GetMapping("/series/category/table")
  @Operation(summary = "그래프 - 연도별 카테고리별 소매, 도매 가격 조회", description = "5년치 카테고리별 소매, 도매 가격 조회")
  public Object getCategorySeriesTable(
      @RequestParam(name = "market", required = false) String market,
      @RequestParam(name = "category", required = false) String category) {

    // 둘 다 없으면: 소매/도매 × 100~600 전부를 "행 배열"로
    if ((market == null || market.isBlank()) && (category == null || category.isBlank())) {
      List<Map<String, Object>> rows = seriesService.getCategorySeriesTable(null);
      return rows; // 메타 → 연도 순서 유지 (LinkedHashMap)
    }
    // market만 있으면: 그 시장의 모든 카테고리 행 배열
    if (category == null || category.isBlank()) {
      List<Map<String, Object>> rows = seriesService.getCategorySeriesTable(market);
      return rows;
    }
    // market + category가 모두 있으면: 한 줄만
    Map<String, Object> row = seriesService.getCategorySeriesTableOne(market, category);
    return row;
  }
}
