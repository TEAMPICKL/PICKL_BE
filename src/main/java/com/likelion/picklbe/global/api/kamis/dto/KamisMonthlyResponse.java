package com.likelion.picklbe.global.api.kamis.dto;

import java.util.List;

import lombok.Data;

@Data
public class KamisMonthlyResponse {

  private String error_code; // "000" 이면 성공
  private Price price; // 결과 본문

  @Data
  public static class Price {

    private String productclscode; // "01"=소매, "02"=도매
    private String caption; // 예: "중도매인 판매가격 > 식량작물 ..."
    private List<MonthlyRow> item; // 보통 1개 (yyyy 기준)
  }

  @Data
  public static class MonthlyRow {

    private String yyyy;
    private String m1;
    private String m2;
    private String m3;
    private String m4;
    private String m5;
    private String m6;
    private String m7;
    private String m8;
    private String m9;
    private String m10;
    private String m11;
    private String m12;
    private String yearavg;
  }
}
