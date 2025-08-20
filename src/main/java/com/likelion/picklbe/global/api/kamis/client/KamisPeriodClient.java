package com.likelion.picklbe.global.api.kamis.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class KamisPeriodClient {

  @Value("${kamis.api.key}")
  private String certKey;

  @Value("${kamis.api.id}")
  private String certId;

  private final WebClient kamisWebClient =
      WebClient.builder().baseUrl("https://www.kamis.or.kr/service/price/xml.do").build();

  /**
   * KAMIS 일별 품목별 도·소매가격(periodProductList) - 원문 그대로(String) 반환
   *
   * @param productClsCode "01"(소매) / "02"(도매)
   * @param convertKgYn "Y" or "N"
   */
  public String fetchPeriodProductListRaw(
      String startDay,
      String endDay,
      String productClsCode,
      String itemCategoryCode,
      String itemCode,
      String kindCode,
      String productRankCode,
      String countyCode,
      String convertKgYn) {
    return kamisWebClient
        .get()
        .uri(
            uri ->
                uri.queryParam("action", "periodProductList")
                    .queryParam("p_returntype", "json")
                    .queryParam("p_cert_key", certKey)
                    .queryParam("p_cert_id", certId)
                    .queryParam("p_startday", startDay)
                    .queryParam("p_endday", endDay)
                    // 일부 문서/샘플은 p_productsclscode 로 표기됨. 샘플 URL과 동일 키 사용
                    .queryParam("p_productsclscode", productClsCode)
                    .queryParam("p_itemcategorycode", itemCategoryCode)
                    .queryParam("p_itemcode", itemCode)
                    .queryParam("p_kindcode", kindCode)
                    .queryParam("p_productrankcode", productRankCode)
                    .queryParam("p_countycode", countyCode)
                    .queryParam("p_convert_kg_yn", convertKgYn)
                    .build())
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }
}
