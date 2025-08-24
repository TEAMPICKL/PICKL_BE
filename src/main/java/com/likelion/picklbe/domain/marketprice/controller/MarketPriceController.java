package com.likelion.picklbe.domain.marketprice.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @PostMapping
  @Operation(
      summary = "전통시장, 대형 마트 가격 수동 입력 api",
      description =
          """
            전통시장/대형마트 가격과 이미지 URL, 상품번호(productNo)를 수동 입력합니다.
            상품명과 단위는 개별 식재료 가격 조회와 동일해야 합니다.
            필드: productName, unit, marketPrice, superMarketPrice, imageUrl(옵션), productNo(옵션)
          """)
  public ResponseEntity<BaseResponse<MarketPrice>> upsert(
      @RequestParam String productName,
      @RequestParam String unit,
      @RequestParam double marketPrice,
      @RequestParam double superMarketPrice,
      @RequestParam(required = false) String imageUrl,
      @RequestParam(required = false) String productNo // ✅ 추가
      ) {
    return ResponseEntity.ok(
        BaseResponse.success(
            "UPSERTED",
            service.upsert(productName, unit, marketPrice, superMarketPrice, imageUrl, productNo)));
  }

  @PatchMapping("/{id}")
  @Operation(
      summary = "수동가 부분 수정(PATCH)",
      description =
          """
            필요한 필드만 전달해 부분적으로 수정합니다.
            전달하지 않은 필드는 그대로 유지됩니다.
            허용 파라미터: productName, unit, marketPrice, superMarketPrice, imageUrl, productNo
          """)
  public ResponseEntity<BaseResponse<MarketPrice>> patch(
      @PathVariable Long id,
      @RequestParam(required = false) String productName,
      @RequestParam(required = false) String unit,
      @RequestParam(required = false) Double marketPrice,
      @RequestParam(required = false) Double superMarketPrice,
      @RequestParam(required = false) String imageUrl,
      @RequestParam(required = false) String productNo) {
    MarketPrice updated =
        service.patch(id, productName, unit, marketPrice, superMarketPrice, imageUrl, productNo);
    return ResponseEntity.ok(BaseResponse.success("UPDATED", updated));
  }
}
