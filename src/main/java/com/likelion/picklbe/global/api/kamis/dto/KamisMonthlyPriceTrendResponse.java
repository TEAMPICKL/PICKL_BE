package com.likelion.picklbe.global.api.kamis.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

/**
 * KAMIS 월별 가격 트렌드 응답 DTO - 루트에 error_code가 온다. - condition은 에코 파라미터만 올 수 있다. - price 배열은
 * yyyymm/price/max/min 문자열 필드.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KamisMonthlyPriceTrendResponse {

  /** 루트에 오는 에러 코드 (정상은 "000") */
  @JsonProperty("error_code")
  private String errorCode;

  /** 에코 파라미터용 condition (code/message가 아예 안 올 수 있음) */
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Data
  public static class Condition {

    public String code; // 옵션
    public String message; // 옵션

    // ✅ 에코 파라미터
    @JsonProperty("p_productno")
    public String pProductNo;

    @JsonProperty("p_regday")
    public String pRegday;

    @JsonProperty("p_cert_key")
    public String pCertKey;

    @JsonProperty("p_cert_id")
    public String pCertId;

    @JsonProperty("p_returntype")
    public String pReturnType;
  }

  /** 월별 데이터 한 행 */
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Data
  public static class Price {

    private String yyyymm; // "202508"
    private String price; // 평균
    private String max; // 최고
    private String min; // 최저

    // yyyymm이 "" 또는 [] 로 오는 경우 방어
    @JsonProperty("yyyymm")
    public void setYyyymm(JsonNode node) {
      this.yyyymm = (node != null && node.isTextual()) ? node.asText() : null;
    }
  }

  private List<Condition> condition;
  private List<Price> price;
}
