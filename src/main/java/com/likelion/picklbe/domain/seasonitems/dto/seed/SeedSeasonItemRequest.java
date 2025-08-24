package com.likelion.picklbe.domain.seasonitems.dto.seed;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeedSeasonItemRequest {

  @NotBlank private String itemname;

  @Min(1)
  @Max(12)
  private int seasonMonth;

  /** 기존 레시피 대체 여부 (true=replace, false=append). 기본 true 권장 */
  private Boolean replace = Boolean.TRUE;

  /** 미리보기만(저장 안 함) 보고 싶으면 true */
  private Boolean dryRun = Boolean.FALSE;
}
