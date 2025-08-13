package com.likelion.picklbe.domain.seasonitems.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.seasonitems.dto.request.SeasonItemCreateRequest;
import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemDetailDto;
import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemSummaryDto;
import com.likelion.picklbe.domain.seasonitems.entity.SeasonItem;
import com.likelion.picklbe.domain.seasonitems.exception.SeasonItemErrorCode;
import com.likelion.picklbe.domain.seasonitems.mapper.SeasonItemMapper;
import com.likelion.picklbe.domain.seasonitems.repository.SeasonItemRepository;
import com.likelion.picklbe.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonItemService {

  private final SeasonItemRepository seasonItemRepository;
  private final SeasonItemMapper seasonItemMapper;

  public List<SeasonItemSummaryDto> getAllSeasonItems() {
    List<SeasonItem> seasonItems = seasonItemRepository.findAll();
    return seasonItemMapper.toSummaryDtoList(seasonItems);
  }

  public SeasonItemDetailDto getSeasonItemById(Long id) {
    SeasonItem seasonItem =
        seasonItemRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(SeasonItemErrorCode.SEASON_ITEM_NOT_FOUND));
    return seasonItemMapper.toDetailDto(seasonItem);
  }

  @Transactional
  public SeasonItemDetailDto create(SeasonItemCreateRequest req) {
    SeasonItem entity =
        SeasonItem.builder()
            .itemname(req.getItemname())
            .shortDescription(req.getShortDescription())
            .representativeNutrient(req.getRepresentativeNutrient())
            .calorie(req.getCalorie())
            .inSeasonMonth(req.getSeasonMonth())
            .howToChoose(req.getHowToChoose())
            .howToStore(req.getHowToStore())
            .howToTrim(req.getHowToTrim())
            .tip(req.getTip())
            .imageUrl(req.getImageUrl())
            .build();

    seasonItemRepository.save(entity);
    return seasonItemMapper.toDetailDto(entity);
  }
}
