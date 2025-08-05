package com.likelion.picklbe.domain.seasonitems.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

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
@Tag(name = "SeasonItem", description = "제철 식재료 관련 API")
public class SeasonItemController {

  private final SeasonItemService seasonItemService;

  @GetMapping
  @Operation(summary = "제철 식재료 목록 조회", description = "제철 식재료의 요약 정보 리스트를 반환합니다.")
  public BaseResponse<List<SeasonItemSummaryDto>> getAllSeasonItems() {
    List<SeasonItemSummaryDto> items = seasonItemService.getAllSeasonItems();
    return BaseResponse.success("제철 식재료 목록 조회 성공", items);
  }

  @GetMapping("/{id}")
  @Operation(summary = "제철 식재료 상세 조회", description = "선택한 제철 식재료의 상세 정보를 반환합니다.")
  public BaseResponse<SeasonItemDetailDto> getSeasonItemById(@PathVariable Long id) {
    SeasonItemDetailDto item = seasonItemService.getSeasonItemById(id);
    return BaseResponse.success("제철 식재료 상세 조회 성공", item);
  }
}
