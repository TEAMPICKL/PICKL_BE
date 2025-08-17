package com.likelion.picklbe.domain.seasonitems.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeasonItemDetailDto {

  @Schema(description = "제철 식재료 ID", example = "1")
  private Long id;

  @Schema(description = "제철 식재료 이름", example = "옥수수")
  private String itemname;

  @Schema(description = "제철 식재료 한 줄 소개", example = "탱글한 식감과 고소함이 살아있는 여름 간식")
  private String shortDescription;

  @Schema(description = "대표 영양소", example = "탄수화물")
  private String representativeNutrient;

  @Schema(description = "칼로리", example = "100g당 86kcal")
  private String calorie;

  @Schema(description = "고르는 방법", example = "껍질이 촉촉하고 선명한 연녹색을 띄는 것")
  private String howToChoose;

  @Schema(description = "보관하는 방법", example = "신문지에 싸서 냉장보관 하세요.")
  private String howToStore;

  @Schema(description = "손질하는 방법", example = "알맹이를 손으로 떼어내는 것을 추천")
  private String howToTrim;

  @Schema(description = "보관 팁", example = "전자레인지로 5분 돌리면 삶지 않아도 OK!")
  private String tip;

  @Schema(description = "식재료 이미지 URL", example = "https://example.com/images/corn.jpg")
  private String imageUrl;

  @Schema(description = "제철 월", example = "8")
  private Integer inSeasonMonth;

  @Schema(description = "단위", example = "100g")
  private String unit;

  @Schema(description = "가격(원)", example = "9000")
  private Integer price;
}
