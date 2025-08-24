package com.likelion.picklbe.domain.seasonitems.dto.seed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeedSeasonItemResponse {

  private Long seasonItemId; // 저장 시 ID, dryRun이면 null
  private String upserted; // "inserted"/"updated" (dryRun이면 null)
  private int recipesInserted; // 저장된 레시피 수 (dryRun이면 0)
  private SeedPreviewDto preview; // 항상 포함(미리보기)
}
