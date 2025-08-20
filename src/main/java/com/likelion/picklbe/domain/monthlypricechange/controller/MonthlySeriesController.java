package com.likelion.picklbe.domain.monthlypricechange.controller;

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

import com.likelion.picklbe.domain.monthlypricechange.dto.MonthlyPriceChangeRawDto;
import com.likelion.picklbe.domain.monthlypricechange.service.MonthlyPriceChangeService;
import com.likelion.picklbe.domain.monthlypricechange.service.MonthlySeriesPersistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/kamis/monthly")
@Tag(name = "Monthly", description = "그래프용 12개월 가격(원시/시계열) API")
@RequiredArgsConstructor
@Validated
public class MonthlySeriesController {

  private final MonthlyPriceChangeService rawService; // RAW(원시) 조회
  private final MonthlySeriesPersistService seriesService; // SERIES(적재/조회)

  // -----------------------------
  // RAW (원시 데이터)
  // -----------------------------
  @GetMapping("/raw/batch")
  @Operation(summary = "DEV - 월별 가격 KAMIS 최신 데이터 조회", description = "월별 전체 raw 데이터 조회")
  public Map<String, MonthlyPriceChangeRawDto> getRawBatch(
      @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @RequestParam(name = "market", required = false) String market // "소매" | "도매"
      ) {
    log.info("[MONTHLY][RAW][BATCH] request date={}, market={}", date, market);
    return rawService.getRawBatch(date, market);
  }

  // -----------------------------
  // SERIES (시계열/적재)
  // -----------------------------

  /** 기준월(=date) 포함 과거 11개월까지 총 12개월치: 품목별 max 저장 + 시장×카테고리 월평균(max) 집계 */
  @PostMapping("/series/ingest")
  @Operation(summary = "DEV - 월별 가격 KAMIS 최신 데이터 적재", description = "월별 전체 raw 데이터 적재 및 집계")
  public Map<String, Object> ingest(
      @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @RequestParam(name = "market", required = false) String market // null이면 소매/도매 모두
      ) {
    return seriesService.ingestAll(date, market);
  }

  /** 품목 시계열: productNo 없으면 전체(Map<productNo, Map<yyyymm, max>>), 있으면 해당 품목만 감싸서 반환 */
  @GetMapping("/series/item")
  @Operation(summary = "그래프 - 월별 전체/특정 식재료 가격(최고가) 조회", description = "12개월치 품목별 max 시계열")
  public Map<String, Map<String, Double>> getAllItemSeriesOrOne(
      @RequestParam(name = "productNo", required = false) String productNo) {
    if (productNo == null || productNo.isBlank()) {
      return seriesService.getAllItemSeries();
    }
    return Map.of(productNo, seriesService.getItemSeries(productNo));
  }

  /**
   * 카테고리 시계열(행 피벗) – 응답 한 행에 메타 먼저, 이후 yyyymm 컬럼이 이어짐 - 둘 다 없으면: 소매/도매 × 100~600 전체 리스트(List<Map>)
   * - market만: 해당 시장의 모든 카테고리 리스트(List<Map>) - 둘 다: 한 줄(Map)
   */
  @GetMapping("/series/category/table")
  @Operation(summary = "그래프 - 월별 카테고리별 시계열(피벗형) 조회", description = "메타 필드가 먼저, yyyymm 값이 이어지는 형태")
  public Object getCategorySeriesTable(
      @RequestParam(name = "market", required = false) String market,
      @RequestParam(name = "category", required = false) String category) {
    if ((market == null || market.isBlank()) && (category == null || category.isBlank())) {
      List<Map<String, Object>> rows = seriesService.getCategorySeriesTable(null);
      return rows;
    }
    if (category == null || category.isBlank()) {
      List<Map<String, Object>> rows = seriesService.getCategorySeriesTable(market);
      return rows;
    }
    Map<String, Object> row = seriesService.getCategorySeriesTableOne(market, category);
    return row;
  }
}
