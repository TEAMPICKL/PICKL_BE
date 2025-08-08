package com.likelion.picklbe.global.api.market.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class VWorldResponse {
  private Response response; // 최상위는 항상 "response" 하나

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Response {
    private String status; // "OK" 또는 "ERROR"
    private Page page; // 페이지 정보
    private Result result; // 실제 데이터
    private ErrorInfo error; // 에러일 때 내려오는 객체 (선택)
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Page {
    private int total;
    private int current;
    private int size;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Result {
    private FeatureCollection featureCollection;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  @NoArgsConstructor
  public static class FeatureCollection {
    private String type; // "FeatureCollection"
    private List<Feature> features;
    // bbox 등 필요하면 추가
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Feature {
    private Geometry geometry; // 포인트 좌표
    private Map<String, Object> properties; // 시장명 등 속성
    private String id;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Geometry {
    private String type; // "Point"
    private List<Double> coordinates; // [lng, lat]
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  @NoArgsConstructor
  public static class ErrorInfo {
    private String level;
    private String code;
    private String text;
  }
}
