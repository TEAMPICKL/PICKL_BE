package com.likelion.picklbe.global.api.market.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;

@Getter
public class VWorldResponse {
  private String status;
  private Response response;

  @Getter
  public static class Response {
    private Result result;
    private Page page;
  }

  @Getter
  public static class Page {
    private int total;
    private int current;
    private int size;
  }

  @Getter
  public static class Result {
    private FeatureCollection featureCollection;
  }

  @Getter
  public static class FeatureCollection {
    private List<Feature> features;
  }

  @Getter
  public static class Feature {
    private Geometry geometry;
    private Map<String, Object> properties;
    private String id;
  }

  @Getter
  public static class Geometry {
    private String type; // "Point"
    private List<Double> coordinates; // [lng, lat]
  }
}
