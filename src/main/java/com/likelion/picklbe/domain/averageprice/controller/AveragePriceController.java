package com.likelion.picklbe.domain.averageprice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.averageprice.response.CategoryAveragePriceResponse;
import com.likelion.picklbe.domain.averageprice.response.ItemPriceResponse;
import com.likelion.picklbe.domain.averageprice.service.AveragePriceService;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/average-price")
@RequiredArgsConstructor
public class AveragePriceController {

  private final AveragePriceService averagePriceService;

  @GetMapping("/category")
  @Operation(summary = "카테고리별 평균 가격 조회", description = "식재료의 카테고리별 평균 가격을 계산하여 반환합니다.")
  public BaseResponse<List<CategoryAveragePriceResponse>> getCategoryAverages() {
    return BaseResponse.success(
        "카테고리별 평균 가격이 성공적으로 조회되었습니다.", averagePriceService.getCategoryAverages());
  }

  @GetMapping("/items")
  @Operation(summary = "개별 식재료 가격 조회", description = "전체 식재료의 최신 가격 및 전일 가격 정보를 반환합니다")
  public BaseResponse<List<ItemPriceResponse>> getItemPrices() {
    return BaseResponse.success("식재료별 가격 정보가 성공적으로 조회되었습니다.", averagePriceService.getItemPrices());
  }

  @GetMapping("/image")
  @Operation(
      summary = "(TEST) 품목 이미지 미리보기",
      description = "쿼리스트링 name으로 품목명을 보내면 Unsplash 첫 이미지 URL을 반환합니다. 예) name=배추/여름(고랭지)")
  public BaseResponse<Map<String, String>> previewImage(@RequestParam("name") String name) {
    String url = averagePriceService.previewImageUrl(name);
    return BaseResponse.success("ok", Map.of("query", name, "imageUrl", url));
  }
}
