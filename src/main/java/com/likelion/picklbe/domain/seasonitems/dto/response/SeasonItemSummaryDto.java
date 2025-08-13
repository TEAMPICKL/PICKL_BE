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
public class SeasonItemSummaryDto {

  @Schema(description = "제철 식재료 ID", example = "1")
  private Long id;

  @Schema(description = "제철 식재료 이름", example = "옥수수")
  private String itemname;

  @Schema(description = "제철 식재료 한 줄 소개", example = "지금이 딱 제철이에요!")
  private String shortDescription;

  @Schema(description = "제철 월", example = "8")
  private Integer seasonMonth;

  @Schema(description = "식재료 이미지 URL", example = "https://example.com/images/corn.jpg")
  private String imageUrl;
}
