package com.likelion.picklbe.domain.seasonitems.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemDetailDto;
import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemSummaryDto;
import com.likelion.picklbe.domain.seasonitems.entity.SeasonItem;

@Component
public class SeasonItemMapper {

  public SeasonItemSummaryDto toSummaryDto(SeasonItem s) {
    return SeasonItemSummaryDto.builder()
        .id(s.getId())
        .itemname(s.getItemname())
        .shortDescription(s.getShortDescription())
        .seasonMonth(s.getInSeasonMonth())
        .imageUrl(s.getImageUrl())
        .unit(s.getUnit())
        .price(s.getPrice())
        .build();
  }

  public SeasonItemDetailDto toDetailDto(SeasonItem s) {
    return SeasonItemDetailDto.builder()
        .id(s.getId())
        .itemname(s.getItemname())
        .shortDescription(s.getShortDescription())
        .representativeNutrient(s.getRepresentativeNutrient())
        .calorie(s.getCalorie())
        .howToChoose(s.getHowToChoose())
        .howToStore(s.getHowToStore())
        .howToTrim(s.getHowToTrim())
        .tip(s.getTip())
        .imageUrl(s.getImageUrl())
        .inSeasonMonth(s.getInSeasonMonth())
        .unit(s.getUnit())
        .price(s.getPrice())
        .build();
  }

  public List<SeasonItemSummaryDto> toSummaryDtoList(List<SeasonItem> list) {
    return list.stream().map(this::toSummaryDto).collect(Collectors.toList());
  }
}
