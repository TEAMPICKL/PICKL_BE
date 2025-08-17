package com.likelion.picklbe.domain.dailypricechange.controller;

import com.likelion.picklbe.domain.dailypricechange.entity.KamisRawPayload;
import com.likelion.picklbe.domain.dailypricechange.response.CategoryDailyPriceChangeResponse;
import com.likelion.picklbe.domain.dailypricechange.response.ItemDailyPriceChangeResponse;
import com.likelion.picklbe.domain.dailypricechange.service.DailyPriceChangePersistService;
import com.likelion.picklbe.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-price-change/store")
@RequiredArgsConstructor
@Tag(name = "daily-price-change-store", description = "KAMIS 저장/조회 API")
public class DailyPriceStoreController {

  private final DailyPriceChangePersistService service;

  @PostMapping("/ingest")
  @Operation(
      summary = "KAMIS 최신 데이터 적재",
      description = "KAMIS에서 불러온 원본/품목/카테고리 데이터를 DB에 저장합니다. date 미지정 시 KST 오늘 기준.")
  public BaseResponse<Map<String, Object>> ingest(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate date) {

    var res = service.ingestLatest(date);
    return BaseResponse.success(
        "ingested",
        Map.of(
            "rawId",
            res.rawId(),
            "itemCount",
            res.itemCount(),
            "categoryCount",
            res.categoryCount()));
  }

  @GetMapping("/items")
  @Operation(summary = "저장된 품목 리스트 조회", description = "date=YYYY-MM-DD, cls=소매/도매(옵션)")
  public BaseResponse<List<ItemDailyPriceChangeResponse>> items(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) String cls) {
    return BaseResponse.success("ok", service.getStoredItems(date, cls));
  }

  @GetMapping("/category")
  @Operation(summary = "저장된 카테고리 평균 조회", description = "date=YYYY-MM-DD, cls=소매/도매(옵션)")
  public BaseResponse<List<CategoryDailyPriceChangeResponse>> category(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) String cls) {
    return BaseResponse.success("ok", service.getStoredCategories(date, cls));
  }

  @GetMapping("/raw/latest")
  @Operation(
      summary = "저장된 원본 JSON 최신건",
      description = "date=YYYY-MM-DD 의 가장 최근 수집 건을 반환합니다(원본 payload 포함).")
  public BaseResponse<Map<String, Object>> rawLatest(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    Optional<KamisRawPayload> opt = service.getLatestRaw(date);
    if (opt.isEmpty()) {
      return BaseResponse.success("no-data", Map.of());
    }
    var r = opt.get();
    return BaseResponse.success(
        "ok",
        Map.of(
            "id", r.getId(),
            "priceDate", r.getPriceDate(),
            "fetchedAt", r.getFetchedAt(),
            "contentHash", r.getContentHash(),
            "payload", r.getPayload()));
  }

//  // 이름으로 검색: /api/daily-price-change/store/items?date=2025-08-17&q=배추&cls=소매
//  @GetMapping(value = "/items", params = "q")
//  @Operation(
//      summary = "저장된 품목 검색(상품명)",
//      description = "date=YYYY-MM-DD, q=검색어(부분일치, 대소문자무시), cls=소매/도매(옵션)")
//  public BaseResponse<List<ItemDailyPriceChangeResponse>> searchItemsByName(
//      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
//      @RequestParam String q,
//      @RequestParam(required = false) String cls) {
//    String query = (q == null) ? "" : q.trim();
//    if (query.isBlank()) {
//      return BaseResponse.success("ok", List.of()); // 빈 검색어면 빈 리스트
//    }
//    return BaseResponse.success("ok", service.searchStoredItems(date, cls, query));
//  }
//
//  @GetMapping(value = "/items", params = "productNo")
//  @Operation(
//      summary = "저장된 품목 검색(productNo)",
//      description = "date=YYYY-MM-DD, productNo=정확일치, cls=소매/도매(옵션)")
//  public BaseResponse<List<ItemDailyPriceChangeResponse>> searchItemsByProductNo(
//      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
//      @RequestParam String productNo,
//      @RequestParam(required = false) String cls) {
//    return BaseResponse.success("ok", service.findByProductNo(date, cls, productNo.trim()));
//  } 

  @GetMapping("/items/search")
  @Operation(
      summary = "저장된 품목 검색(상품명 부분일치)",
      description = "q=검색어(부분일치, 대소문자 무시). date, cls는 옵션. date가 없으면 최신 수집일 사용")
  public BaseResponse<List<ItemDailyPriceChangeResponse>> searchItemsByName(
      @RequestParam String q,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) String cls) {
    return BaseResponse.success("ok", service.searchByName(date, cls, q));
  }
}
