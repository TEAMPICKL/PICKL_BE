package com.likelion.picklbe.domain.seasonitems.dto.seed;

import java.util.List;
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
public class SeedPreviewDto {

  private String shortDescription;
  private String representativeNutrient;
  private String howToChoose;
  private String howToStore;
  private String howToTrim;
  private String tip;
  private List<SeedRecipePreviewDto> recipes;
}
