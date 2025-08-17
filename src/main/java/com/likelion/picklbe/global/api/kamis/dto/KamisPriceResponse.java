package com.likelion.picklbe.global.api.kamis.dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
public class KamisPriceResponse {

  @JsonProperty("condition")
  private List<List<String>> condition;

  @JsonProperty("price")
  private List<Item> price;

  @JsonProperty("error_code")
  private String errorCode;

  @Data
  public static class Item {

    @JsonProperty("product_cls_code")
    private String productClsCode;

    @JsonProperty("product_cls_name")
    private String productClsName;

    @JsonProperty("category_code")
    private String categoryCode;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("productno")
    @JsonAlias({"product_no", "productNo"}) // <- 추가하면 포맷 바뀌어도 매핑됨
    private String productNo;

    @JsonProperty("lastest_day")
    private String latestDay;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("day1")
    private String day1;

    @JsonProperty("dpr1")
    @JsonDeserialize(using = StringListDeserializer.class)
    private List<String> latestPrice;

    @JsonProperty("day2")
    private String day2;

    @JsonProperty("dpr2")
    @JsonDeserialize(using = StringListDeserializer.class)
    private List<String> oneDayAgoPrice;

    @JsonProperty("day3")
    private String day3;

    @JsonProperty("dpr3")
    @JsonDeserialize(using = StringListDeserializer.class)
    private List<String> oneMonthAgoPrice;

    @JsonProperty("day4")
    private String day4;

    @JsonProperty("dpr4")
    @JsonDeserialize(using = StringListDeserializer.class)
    private List<String> oneYearAgoPrice;

    @JsonProperty("direction")
    @JsonDeserialize(using = StringListDeserializer.class)
    private List<String> direction;

    @JsonProperty("value")
    @JsonDeserialize(using = StringListDeserializer.class)
    private List<String> value;
  }

  /** 배열이면 풀고, 단일이면 하나짜리 리스트에 담아서 반환 */
  public static class StringListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      List<String> list = new ArrayList<>();
      if (p.isExpectedStartArrayToken()) {
        while (p.nextToken() != JsonToken.END_ARRAY) {
          list.add(p.getValueAsString());
        }
      } else {
        list.add(p.getValueAsString());
      }
      return list;
    }
  }
}
