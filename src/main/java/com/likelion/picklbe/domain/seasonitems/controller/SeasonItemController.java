package com.likelion.picklbe.domain.seasonitems.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.seasonitems.dto.request.SeasonItemCreateRequest;
import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemDetailDto;
import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemSummaryDto;
import com.likelion.picklbe.domain.seasonitems.service.SeasonItemService;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/season-items")
@RequiredArgsConstructor
@Tag(name = "SeasonItem", description = "메인 페이지 - 제철 식재료 관련 API")
public class SeasonItemController {

  private final SeasonItemService seasonItemService;

  @GetMapping
  @Operation(
      summary = "제철 식재료 목록 조회",
      description =
          """
              제철 식재료의 요약 정보 리스트를 반환합니다\n
              필드: 제철 식재료 이름(itemname), 한 줄 소개(shortDescription), 제철 월(seasonMonth), 식재료 이미지 url(imageUrl)
              """)
  public BaseResponse<List<SeasonItemSummaryDto>> getAllSeasonItems() {
    List<SeasonItemSummaryDto> items = seasonItemService.getAllSeasonItems();
    return BaseResponse.success("제철 식재료 목록 조회 성공", items);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "제철 식재료 상세 조회",
      description =
          """
              제철 식재료의 상제 정보 리스트를 반환합니다\n
              필드: 제철 식재료 이름(itemname), 한 줄 소개(shortDescription), 대표 영양소(representativeNutrient), 고르는 방법(howToChoose), 보관하는 방법(howToStore), 손질하는 방법(howToTrim), 보관팁(tip), 식재료 이미지 url(imageUrl)
              """)
  public BaseResponse<SeasonItemDetailDto> getSeasonItemById(@PathVariable Long id) {
    SeasonItemDetailDto item = seasonItemService.getSeasonItemById(id);
    return BaseResponse.success("제철 식재료 상세 조회 성공", item);
  }

  @PostMapping
  @Operation(summary = "Dev 제철 식재료 등록", description = "제철 식재료 정보 직접 넣어 등록")
  public BaseResponse<SeasonItemDetailDto> create(@RequestBody @Valid SeasonItemCreateRequest req) {
    return BaseResponse.success("제철 식재료 등록 성공", seasonItemService.create(req));
  }
}
