package com.likelion.picklbe.domain.dailypricechange.controller;

import com.likelion.picklbe.domain.dailypricechange.entity.KamisRawPayload;
import com.likelion.picklbe.domain.dailypricechange.response.CategoryDailyPriceChangeResponse;
import com.likelion.picklbe.domain.dailypricechange.response.ItemDailyPriceChangeResponse;
import com.likelion.picklbe.domain.dailypricechange.service.DailyPriceChangePersistService;
import com.likelion.picklbe.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
      summary = "DEV - KAMIS 최신 데이터 적재",
      description = "KAMIS에서 불러온 원본/품목/카테고리 데이터를 DB에 저장합니다. date 미지정 시 KST 오늘 기준.")
  public BaseResponse<Map<String, Object>> ingest(
      @Parameter(description = "적재 대상 날짜(생략 시 오늘)")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate date) {

    var res = service.ingestLatest(date);
    return BaseResponse.success(
        "ingested",
        Map.of(
            "rawId", res.rawId(),
            "itemCount", res.itemCount(),
            "categoryCount", res.categoryCount()));
  }

  @PostMapping("/images/ingest")
  @Operation(
      summary = "이미지 URL 적재(미채움 우선, 필요 시 1번부터 리프레시)",
      description =
          """
              imageUrl이 NULL인 레코드를 id 오름차순으로 최대 batchSize개(기본 50)까지 Unsplash에서 검색해 채웁니다.
              모두 채워진 경우 자동으로 1번부터 N개를 재검사하여, 변경사항이 있으면 업데이트합니다.
              date, market(소매/도매)은 옵션이며, date 생략 시 최신 수집일을 사용합니다.
              강제로 리프레시 모드로만 수행하려면 refresh=true를 주면 됩니다.
              """)
  public BaseResponse<Map<String, Object>> ingestImages(
      @RequestParam(required = false) Integer batchSize,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(name = "market", required = false) String market,
      @RequestParam(name = "refresh", required = false, defaultValue = "false") boolean refresh) {
    Map<String, Object> res = service.ingestMissingImages(batchSize, date, market, refresh);
    return BaseResponse.success("image-ingested", res);
  }

  @GetMapping("/items")
  @Operation(
      summary = "PICK - 저장된 품목 리스트 조회",
      description = "date=YYYY-MM-DD(옵션), market=소매|도매(옵션). date 생략 시 최신 수집일 사용")
  public BaseResponse<List<ItemDailyPriceChangeResponse>> items(
      @Parameter(description = "조회 날짜(생략 시 최신 수집일)")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate date,
      @Parameter(description = "시장 구분", example = "소매")
      @RequestParam(name = "market", required = false)
      String market) {

    return BaseResponse.success("ok", service.getStoredItems(date, market));
  }

  @GetMapping("/category")
  @Operation(
      summary = "메인화면/ 식세평균 - 저장된 카테고리 평균 조회",
      description = "date=YYYY-MM-DD(옵션), market=소매|도매(옵션). date 생략 시 최신 수집일 사용")
  public BaseResponse<List<CategoryDailyPriceChangeResponse>> category(
      @Parameter(description = "조회 날짜(생략 시 최신 수집일)")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate date,
      @Parameter(
          description = "시장 구분",
          example = "도매",
          examples = {
              @ExampleObject(name = "Retail", value = "소매"),
              @ExampleObject(name = "Wholesale", value = "도매")
          })
      @RequestParam(name = "market", required = false)
      String market) {

    return BaseResponse.success("ok", service.getStoredCategories(date, market));
  }

  @GetMapping("/items/search")
  @Operation(
      summary = "PICK - 저장된 품목 검색 (상품명 부분일치)",
      description = "name=상품명 키워드(부분일치, 대소문자 무시). date, market(소매/도매)은 옵션. date 생략 시 최신 수집일 사용")
  public BaseResponse<List<ItemDailyPriceChangeResponse>> searchItemsByName(
      @Parameter(description = "상품명 검색어(부분일치)", example = "배추") @RequestParam(name = "name")
      String name,
      @Parameter(description = "조회 날짜(생략 시 최신 수집일)")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate date,
      @Parameter(
          description = "시장 구분",
          example = "소매",
          examples = {
              @ExampleObject(name = "Retail", value = "소매"),
              @ExampleObject(name = "Wholesale", value = "도매")
          })
      @RequestParam(name = "market", required = false)
      String market) {

    String keyword = name == null ? "" : name.trim();
    if (keyword.isBlank()) {
      return BaseResponse.success("ok", List.of());
    }
    return BaseResponse.success("ok", service.searchByName(date, market, keyword));
  }

  @GetMapping("/items/{id}")
  @Operation(summary = "PICK - ID로 단일 품목 조회", description = "kamis_item_price의 PK(ID)로 단건 조회")
  public BaseResponse<ItemDailyPriceChangeResponse> getItemById(
      @Parameter(description = "품목 PK", example = "12345") @PathVariable Long id) {

    return service
        .getItemById(id)
        .map(r -> BaseResponse.success("ok", r))
        .orElseGet(() -> BaseResponse.success("not-found", null));
  }

  @GetMapping("/raw/latest")
  @Operation(
      summary = "DEV - 저장된 원본 JSON 최신건",
      description = "date=YYYY-MM-DD 의 가장 최근 수집 건을 반환(원본 payload 포함)")
  public BaseResponse<Map<String, Object>> rawLatest(
      @Parameter(description = "조회 날짜")
      @RequestParam
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate date) {

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
}
