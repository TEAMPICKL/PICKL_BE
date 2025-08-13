package com.likelion.picklbe.domain.marketprice.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.marketprice.dto.response.MarketPriceResponse;
import com.likelion.picklbe.domain.marketprice.entity.MarketPrice;
import com.likelion.picklbe.domain.marketprice.service.MarketPriceService;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market-prices")
@Tag(name = "marketprice-controller", description = "메인 페이지 - 전통시장, 대형 마트 가격 비교 API")
public class MarketPriceController {

  private final MarketPriceService service;

  @Operation(
      summary = "전통시장, 대형 마트 가격 비교 ",
      description =
          """
              메인 페이지/알뜰소비 api\n
              파라미터 없이 호출하면: DB에 수동으로 넣어둔 모든 (상품명, 단위) 가격을 반환\n
              필드: 상품명(productName), 단위(unit), 전통시장 가격(marketPrice), 대형마트 가격(superMarketPrice)
              """)
  @GetMapping
  public ResponseEntity<BaseResponse<List<MarketPriceResponse>>> list(
      @RequestParam(required = false) Set<String> names,
      @RequestParam(required = false) Set<String> keys) {
    return ResponseEntity.ok(
        BaseResponse.success("OK", service.getMarketPricesFiltered(names, keys, true)));
  }

  @Operation(
      summary = "전통시장, 대형 마트 가격 수동 입력 api",
      description =
          """
              전통시장 가격, 대형마트 가격 입력을 위한 api\n
              상품명과 단위가 개별 식재료 가격 조회에 나와 있는 정보가 정확히 일치해야함\n
              필드: 상품명(productName), 단위(unit), 전통시장 가격(marketPrice), 대형마트 가격(superMarketPrice)
              """)
  @PostMapping
  public ResponseEntity<BaseResponse<MarketPrice>> upsert(
      @RequestParam String productName,
      @RequestParam String unit,
      @RequestParam double marketPrice,
      @RequestParam double superMarketPrice) {
    return ResponseEntity.ok(
        BaseResponse.success(
            "UPSERTED", service.upsert(productName, unit, marketPrice, superMarketPrice)));
  }
}
