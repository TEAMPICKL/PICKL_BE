package com.likelion.picklbe.domain.dailypricechange.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.dailypricechange.response.CategoryDailyPriceChangeResponse;
import com.likelion.picklbe.domain.dailypricechange.response.ItemDailyPriceChangeResponse;
import com.likelion.picklbe.domain.dailypricechange.service.DailyPriceChangeService;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/daily-price-change")
@RequiredArgsConstructor
@Tag(name = "daily-price-change", description = "전일 대비 가격 변동 API")
public class DailyPriceChangeController {

  private final DailyPriceChangeService dailyPriceChangeService;

  @GetMapping("/category")
  @Operation(
      summary = "부류코드별 당일 가격, 전날 가격, 등락률 조회",
      description = "전체 부류코드별 당일 가격, 전날 가격, 등락률을 계산하여 반환합니다.")
  public BaseResponse<List<CategoryDailyPriceChangeResponse>> getCategoryAverages() {
    return BaseResponse.success(
        "카테고리별 평균 가격이 성공적으로 조회되었습니다.", dailyPriceChangeService.getCategoryAverages());
  }

  @GetMapping("/items")
  @Operation(
      summary = "개별 식재료 단위, 당일 가격 ,전날 가격, 등락률 조회",
      description = "전체 식재료의 단위, 최신 가격, 전일 가격 정보 등락률을 반환합니다")
  public BaseResponse<List<ItemDailyPriceChangeResponse>> getItemPrices() {
    return BaseResponse.success(
        "식재료별 가격 정보가 성공적으로 조회되었습니다.", dailyPriceChangeService.getItemPrices());
  }

  @GetMapping("/image")
  @Operation(
      summary = "(TEST) 품목 이미지 미리보기",
      description = "쿼리스트링 name으로 품목명을 보내면 Unsplash 첫 이미지 URL을 반환합니다. 예) name=배추/여름(고랭지)")
  public BaseResponse<Map<String, String>> previewImage(@RequestParam("name") String name) {
    String url = dailyPriceChangeService.previewImageUrl(name);
    return BaseResponse.success("ok", Map.of("query", name, "imageUrl", url));
  }
}
