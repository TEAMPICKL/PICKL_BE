package com.likelion.picklbe.domain.yearlypricechange.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** 메타(시장/카테고리) 먼저, 연도 필드들이 그 뒤에 이어지도록 보장하는 DTO */
@JsonPropertyOrder({"productClsName", "categoryCode", "categoryName"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YearlyCategorySeriesRow {

  private final String productClsName;
  private final String categoryCode;
  private final String categoryName;

  // 메타 뒤에 이어질 연도별 값 (삽입 순서 보장)
  private final LinkedHashMap<String, Double> years = new LinkedHashMap<>();

  public YearlyCategorySeriesRow(String productClsName, String categoryCode, String categoryName) {
    this.productClsName = productClsName;
    this.categoryCode = categoryCode;
    this.categoryName = categoryName;
  }

  public String getProductClsName() {
    return productClsName;
  }

  public String getCategoryCode() {
    return categoryCode;
  }

  public String getCategoryName() {
    return categoryName;
  }

  /** 연도 값 추가 */
  public void put(String yyyy, Double value) {
    years.put(yyyy, value);
  }

  /** 메타 다음에 연도 키들을 평탄화해서 이어 붙임 */
  @JsonAnyGetter
  public Map<String, Double> getYears() {
    return years;
  }
}
