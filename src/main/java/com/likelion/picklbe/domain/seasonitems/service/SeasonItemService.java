package com.likelion.picklbe.domain.seasonitems.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.seasonitems.dto.request.SeasonItemCreateRequest;
import com.likelion.picklbe.domain.seasonitems.dto.request.SeasonItemUpdateRequest;
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
public class SeasonItemService {

  private final SeasonItemRepository seasonItemRepository;
  private final SeasonItemMapper seasonItemMapper;

  @Transactional(readOnly = true)
  public List<SeasonItemSummaryDto> getAllSeasonItems() {
    return seasonItemMapper.toSummaryDtoList(seasonItemRepository.findAll());
  }

  @Transactional(readOnly = true)
  public SeasonItemDetailDto getSeasonItemById(Long id) {
    return seasonItemMapper.toDetailDto(getEntity(id));
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
            .unit(req.getUnit())
            .price(req.getPrice()) // int(원)
            .build();

    return seasonItemMapper.toDetailDto(seasonItemRepository.save(entity));
  }

  @Transactional
  public SeasonItemDetailDto update(Long id, SeasonItemUpdateRequest req) {
    SeasonItem cur = getEntity(id);

    // 부분 업데이트: null 아닌 값만 반영해서 새 엔티티로 merge (RecipeService 스타일)
    SeasonItem updated =
        SeasonItem.builder()
            .id(cur.getId())
            .itemname(nvl(req.getItemname(), cur.getItemname()))
            .shortDescription(nvl(req.getShortDescription(), cur.getShortDescription()))
            .representativeNutrient(
                nvl(req.getRepresentativeNutrient(), cur.getRepresentativeNutrient()))
            .calorie(nvl(req.getCalorie(), cur.getCalorie()))
            .inSeasonMonth(nvl(req.getSeasonMonth(), cur.getInSeasonMonth()))
            .howToChoose(nvl(req.getHowToChoose(), cur.getHowToChoose()))
            .howToStore(nvl(req.getHowToStore(), cur.getHowToStore()))
            .howToTrim(nvl(req.getHowToTrim(), cur.getHowToTrim()))
            .tip(nvl(req.getTip(), cur.getTip()))
            .imageUrl(nvl(req.getImageUrl(), cur.getImageUrl()))
            .unit(nvl(req.getUnit(), cur.getUnit()))
            .price(nvl(req.getPrice(), cur.getPrice()))
            .recommendedRecipes(cur.getRecommendedRecipes()) // 연관관계 유지
            .build();

    return seasonItemMapper.toDetailDto(seasonItemRepository.save(updated));
  }

  @Transactional
  public void delete(Long id) {
    SeasonItem cur = getEntity(id);
    seasonItemRepository.delete(cur);
  }

  // ---------- helper ----------
  private SeasonItem getEntity(Long id) {
    return seasonItemRepository
        .findById(id)
        .orElseThrow(() -> new CustomException(SeasonItemErrorCode.SEASON_ITEM_NOT_FOUND));
  }

  private static <T> T nvl(T v, T def) {
    return v != null ? v : def;
  }
}
