package com.likelion.picklbe.global.api.mart.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoCategoryResponse {

  private List<Document> documents;
  private Meta meta;

  @Getter
  @Setter
  public static class Document {

    private String id;

    @JsonProperty("place_name")
    private String placeName;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("category_group_code")
    private String categoryGroupCode; // MT1

    @JsonProperty("category_group_name")
    private String categoryGroupName;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("road_address_name")
    private String roadAddressName;

    // 경도(x), 위도(y) 문자열로 옴
    private String x; // lng
    private String y; // lat
  }

  @Getter
  @Setter
  public static class Meta {

    @JsonProperty("is_end")
    private boolean isEnd;

    @JsonProperty("pageable_count")
    private int pageableCount;

    @JsonProperty("total_count")
    private int totalCount;
  }
}
