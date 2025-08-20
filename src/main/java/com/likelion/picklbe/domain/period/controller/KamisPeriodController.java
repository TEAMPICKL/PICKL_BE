package com.likelion.picklbe.domain.period.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.period.service.KamisPeriodService;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kamis/period")
@RequiredArgsConstructor
@Tag(name = "kamis-period", description = "KAMIS 일별 품목별 도·소매가격(periodProductList) 프리뷰")
public class KamisPeriodController {

  private final KamisPeriodService service;

  @GetMapping("/preview")
  @Operation(
      summary = "단일 조합 프리뷰",
      description =
          "필수: categoryCode,itemCode,kindCode / 옵션: startDay,endDay,market,productRankCode,countyCode")
  public BaseResponse<Map<String, Object>> previewOne(
      @RequestParam String categoryCode,
      @RequestParam String itemCode,
      @RequestParam String kindCode,
      @RequestParam(required = false) String startDay, // 기본 2025-08-01
      @RequestParam(required = false) String endDay, // 기본 오늘
      @RequestParam(required = false) String market, // 소매/도매/01/02
      @RequestParam(required = false) String productRankCode, // 기본 "04"
      @RequestParam(required = false) String countyCode // 기본 "1101"
      ) {
    return BaseResponse.success(
        "ok",
        service.previewOne(
            startDay,
            endDay,
            market,
            categoryCode,
            itemCode,
            kindCode,
            productRankCode,
            countyCode));
  }

  @GetMapping("/preview-all")
  @Operation(
      summary = "전체 프리뷰",
      description = "source=daily(기본) | static(정적 테이블). 기간 기본 2025-08-01~오늘, 마켓 기본 소매/도매 모두")
  public BaseResponse<Map<String, Object>> previewAll(
      @RequestParam(required = false) String startDay,
      @RequestParam(required = false) String endDay,
      @RequestParam(required = false) String categoryCode, // 특정 부류만
      @RequestParam(required = false) String market, // 소매/도매/01/02, 비우면 둘 다
      @RequestParam(required = false) String productRankCode, // 기본 "04"
      @RequestParam(required = false) String countyCode, // 기본 "1101"
      @RequestParam(required = false, defaultValue = "daily") String source) {
    return BaseResponse.success(
        "ok",
        service.previewAll(
            startDay, endDay, categoryCode, market, productRankCode, countyCode, source));
  }
}
