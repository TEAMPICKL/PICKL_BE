package com.likelion.picklbe.global.api.kamis.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KamisYearlyPriceTrendResponse {

  /** 상단 코드 (정상: "000") */
  @JsonProperty("error_code")
  private String errorCode;

  /** 일부 응답에서 쓰이는 대체 코드 키 */
  @JsonProperty("result_code")
  private String resultCode;

  /** 에코/상세 메시지 등이 올 수 있는 블록 */
  private List<Condition> condition;

  /** 연도별 항목 리스트 */
  private List<TrendItem> price;

  /** 편의 메서드: 최상단 코드 → 없으면 condition[0].code */
  public String getTopErrorCode() {
    if (errorCode != null && !errorCode.isBlank()) {
      return errorCode;
    }
    if (resultCode != null && !resultCode.isBlank()) {
      return resultCode;
    }
    if (condition != null && !condition.isEmpty()) {
      return condition.get(0).getCode();
    }
    return null;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Condition {

    private String code;
    private String message;

    @JsonProperty("p_productno")
    private String pProductNo;

    @JsonProperty("p_regday")
    private String pRegday;

    @JsonProperty("p_cert_key")
    private String pCertKey;

    @JsonProperty("p_cert_id")
    private String pCertId;

    @JsonProperty("p_returntype")
    private String pReturnType;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TrendItem {

    /** 연도 (4자리 숫자만 인정, 아니면 null) */
    private String yyyy;

    /** 연간 평균(있을 수도/없을 수도 있음) */
    private String price;

    /** 연간 최고/최저 */
    private String max;

    private String min;

    @JsonProperty("yyyy")
    public void setYyyyNode(JsonNode node) {
      String v = scalar(node);
      this.yyyy = (v != null && v.matches("\\d{4}")) ? v : null;
    }

    @JsonProperty("price")
    public void setPriceNode(JsonNode node) {
      this.price = scalar(node);
    }

    @JsonProperty("max")
    public void setMaxNode(JsonNode node) {
      this.max = scalar(node);
    }

    @JsonProperty("min")
    public void setMinNode(JsonNode node) {
      this.min = scalar(node);
    }

    /** 배열/숫자/문자열을 안전하게 문자열로 변환: [], null → null / [12345] or ["12345"] → "12345" */
    private static String scalar(JsonNode node) {
      if (node == null || node.isNull()) {
        return null;
      }
      if (node.isArray()) {
        if (node.size() == 0) {
          return null;
        }
        node = node.get(0);
        if (node == null || node.isNull()) {
          return null;
        }
      }
      if (node.isNumber() || node.isTextual()) {
        return node.asText();
      }
      return null; // 객체 등은 무시
    }
  }
}
