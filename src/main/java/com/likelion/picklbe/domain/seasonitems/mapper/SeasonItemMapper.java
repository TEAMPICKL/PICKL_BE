package com.likelion.picklbe.domain.seasonitems.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemDetailDto;
import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemSummaryDto;
import com.likelion.picklbe.domain.seasonitems.entity.SeasonItem;

@Component
public class SeasonItemMapper {

  public SeasonItemSummaryDto toSummaryDto(SeasonItem seasonItem) {
    return SeasonItemSummaryDto.builder()
        .id(seasonItem.getId())
        .itemname(seasonItem.getItemname())
        .shortDescription(seasonItem.getShortDescription())
        .imageUrl(seasonItem.getImageUrl())
        .build();
  }

  public SeasonItemDetailDto toDetailDto(SeasonItem seasonItem) {
    return SeasonItemDetailDto.builder()
        .id(seasonItem.getId())
        .itemname(seasonItem.getItemname())
        .shortDescription(seasonItem.getShortDescription())
        .carbohydratePercent(seasonItem.getCarbohydratePercent())
        .proteinPercent(seasonItem.getProteinPercent())
        .fatPercent(seasonItem.getFatPercent())
        .howToChoose(seasonItem.getHowToChoose())
        .howToStore(seasonItem.getHowToStore())
        .howToTrim(seasonItem.getHowToTrim())
        .tip(seasonItem.getTip())
        .imageUrl(seasonItem.getImageUrl())
        .build();
  }

  public List<SeasonItemSummaryDto> toSummaryDtoList(List<SeasonItem> seasonItems) {
    return seasonItems.stream().map(this::toSummaryDto).collect(Collectors.toList());
  }
}
