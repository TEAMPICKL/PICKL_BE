package com.likelion.picklbe.domain.seasonitems.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeasonItemDetailDto {

  @Schema(description = "제철 식재료 ID", example = "1")
  private Long id;

  @Schema(description = "제철 식재료 이름", example = "옥수수")
  private String itemname;

  @Schema(description = "제철 식재료 한 줄 소개", example = "지금이 딱 제철이에요!")
  private String shortDescription;

  @Schema(description = "탄수화물 비율 (%)", example = "76")
  private int carbohydratePercent;

  @Schema(description = "단백질 비율 (%)", example = "13")
  private int proteinPercent;

  @Schema(description = "지방 비율 (%)", example = "11")
  private int fatPercent;

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
}
