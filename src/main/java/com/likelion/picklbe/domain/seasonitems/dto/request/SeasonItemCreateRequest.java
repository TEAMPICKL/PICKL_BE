package com.likelion.picklbe.domain.seasonitems.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class SeasonItemCreateRequest {

  @NotBlank
  @Schema(description = "이름", example = "옥수수")
  private String itemname;

  @NotBlank
  @Schema(description = "한 줄 소개", example = "지금이 딱 제철이에요!")
  private String shortDescription;

  @NotBlank
  @Schema(description = "대표 영양소", example = "탄수화물")
  private String representativeNutrient;

  @NotBlank
  @Schema(description = "칼로리 표기", example = "100g당 86kcal")
  private String calorie;

  @NotNull
  @Min(1)
  @Max(12)
  @Schema(description = "제철 월(1~12)", example = "8")
  private Integer seasonMonth;

  @NotBlank
  @Schema(description = "고르는 방법")
  private String howToChoose;

  @NotBlank
  @Schema(description = "보관하는 방법")
  private String howToStore;

  @NotBlank
  @Schema(description = "손질하는 방법")
  private String howToTrim;

  @NotBlank
  @Schema(description = "팁")
  private String tip;

  @NotBlank
  @Schema(description = "이미지 URL")
  private String imageUrl;
}
