package com.likelion.picklbe.domain.mart.controller;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.likelion.picklbe.domain.marketplace.dto.response.MarketMarkerResponse;
import com.likelion.picklbe.domain.mart.service.MartQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
  @ApiResponse(
      responseCode = "429",
      description = "Kakao Local 레이트리밋(Too Many Requests)",
      content = @Content)
  @ApiResponse(
      responseCode = "504",
      description = "Kakao Local 타임아웃(Gateway Timeout)",
      content = @Content)
  @ApiResponse(
      responseCode = "502",
      description = "Kakao Local 업스트림 오류(Bad Gateway)",
      content = @Content)
  @GetMapping("/api/marts")
  public List<MarketMarkerResponse> getMarts(
      @Parameter(
              description = "경도(lng) - 서쪽 경계",
              schema = @Schema(example = "126.764", defaultValue = "126.764"))
          @RequestParam
          double minX,
      @Parameter(
              description = "위도(lat) - 남쪽 경계",
              schema = @Schema(example = "37.413", defaultValue = "37.413"))
          @RequestParam
          double minY,
      @Parameter(
              description = "경도(lng) - 동쪽 경계",
              schema = @Schema(example = "127.183", defaultValue = "127.183"))
          @RequestParam
          double maxX,
      @Parameter(
              description = "위도(lat) - 북쪽 경계",
              schema = @Schema(example = "37.715", defaultValue = "37.715"))
          @RequestParam
          double maxY,
      @Parameter(
              description = "페이지 (1~45 권장)",
              schema = @Schema(example = "1", defaultValue = "1", minimum = "1", maximum = "45"))
          @RequestParam(required = false, defaultValue = "1")
          @Min(1)
          @Max(45)
          Integer page,
      @Parameter(
              description = "페이지 크기 (1~15 권장)",
              schema = @Schema(example = "15", defaultValue = "15", minimum = "1", maximum = "15"))
          @RequestParam(required = false, defaultValue = "15")
          @Min(1)
          @Max(15)
          Integer size) {

    log.info(
        "[API] /api/marts minX={}, minY={}, maxX={}, maxY={}, page={}, size={}",
        minX,
        minY,
        maxX,
        maxY,
        page,
        size);

    double area = Math.abs(maxX - minX) * Math.abs(maxY - minY);
    if (area > 0.30) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색 범위가 너무 넓어요. 지도를 더 확대해 주세요.");
    }

    return service.getMarts(minX, minY, maxX, maxY, page, size);
  }
}
