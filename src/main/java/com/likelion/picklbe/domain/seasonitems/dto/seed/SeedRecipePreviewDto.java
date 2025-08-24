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
public class SeedRecipePreviewDto {

  private String recipeName;
  private String ingredients;
  private String instructions;
  private String tip;
  private String cookingTimeText;
  private String recommendTagsCsv;
}
