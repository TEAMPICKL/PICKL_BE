package com.likelion.picklbe.domain.seasonitems.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeasonItemUpdateRequest {

  @Schema(description = "이름", example = "옥수수")
  private String itemname;

  @Schema(description = "한 줄 소개", example = "지금이 딱 제철이에요!")
  private String shortDescription;

  @Schema(description = "대표 영양소", example = "탄수화물")
  private String representativeNutrient;

  @Schema(description = "칼로리 표기", example = "100g당 86kcal")
  private String calorie;

  @Min(1)
  @Max(12)
  @Schema(description = "제철 월(1~12)", example = "8")
  private Integer seasonMonth;

  @Schema(description = "고르는 방법")
  private String howToChoose;

  @Schema(description = "보관하는 방법")
  private String howToStore;

  @Schema(description = "손질하는 방법")
  private String howToTrim;

  @Schema(description = "팁")
  private String tip;

  @Schema(description = "이미지 URL")
  private String imageUrl;

  @Schema(description = "단위", example = "100g")
  private String unit;

  @Min(0)
  @Schema(description = "가격(원화 정수)", example = "9000")
  private Integer price;
}
