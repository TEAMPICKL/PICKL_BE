package com.likelion.picklbe.domain.mart.controller;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.likelion.picklbe.domain.marketplace.dto.response.MarketMarkerResponse;
import com.likelion.picklbe.domain.mart.service.MartQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Marts", description = "대형마트(MT1) 조회 API (Kakao Local 프록시)")
public class MartController {

  private final MartQueryService service;

  @Operation(
      summary = "현재 지도 BBOX 내 대형마트 목록",
      description =
          """
          Kakao Local Category(MT1) 검색을 프록시합니다.
          - 좌하단(minX, minY) ~ 우상단(maxX, maxY) BBOX로 조회
          - Kakao Local 쿼터/페이지 규칙을 따릅니다 (page=1..45, size=1..15 권장)
          - 좌표계: WGS84 (경도 lng, 위도 lat)
          """)
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = MarketMarkerResponse.class))))
  @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Kakao Local 서비스 미활성화 또는 권한 문제",
      content = @Content)
  @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
  @Parameters({
    @Parameter(name = "minX", description = "경도(lng) - 서쪽 경계", example = "126.764"),
    @Parameter(name = "minY", description = "위도(lat) - 남쪽 경계", example = "37.413"),
    @Parameter(name = "maxX", description = "경도(lng) - 동쪽 경계", example = "127.183"),
    @Parameter(name = "maxY", description = "위도(lat) - 북쪽 경계", example = "37.715"),
    @Parameter(name = "page", description = "페이지 (기본 1, 1~45 권장)", example = "1"),
    @Parameter(name = "size", description = "페이지 크기 (기본 15, 1~15 권장)", example = "15")
  })
  @GetMapping("/api/marts")
  public List<MarketMarkerResponse> getMarts(
      @RequestParam double minX, // lng west
      @RequestParam double minY, // lat south
      @RequestParam double maxX, // lng east
      @RequestParam double maxY, // lat north
      @RequestParam(required = false, defaultValue = "1") @Min(1) @Max(45) Integer page,
      @RequestParam(required = false, defaultValue = "15") @Min(1) @Max(15) Integer size) {

    log.info(
        "[API] /api/marts minX={}, minY={}, maxX={}, maxY={}, page={}, size={}",
        minX,
        minY,
        maxX,
        maxY,
        page,
        size);

    return service.getMarts(minX, minY, maxX, maxY, page, size);
  }
}
