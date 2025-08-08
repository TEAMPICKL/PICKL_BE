package com.likelion.picklbe.domain.marketplace.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.likelion.picklbe.domain.marketplace.dto.response.MarketMarkerResponse;
import com.likelion.picklbe.domain.marketplace.service.MarketplaceQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "marketplace-controller", description = "전통시장 마커 조회 API")
public class MarketplaceController {

  private final MarketplaceQueryService service;

  @Operation(
      summary = "현재 지도 BBOX 내 전통시장 마커 조회",
      description =
          """
    지도 뷰포트의 경계(BBOX: minX/minY/maxX/maxY)로 VWORLD 전통시장 데이터를 조회한 뒤
    마커 표시에 필요한 최소 정보(id, name, category, address, lat, lng, parking)만 반환합니다.
    - 좌표계: WGS84 (EPSG:4326)
    - page는 1부터 시작, size 기본 50
    """)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                array =
                    @ArraySchema(schema = @Schema(implementation = MarketMarkerResponse.class)))),
    @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
    @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("/api/markets")
  public List<MarketMarkerResponse> getMarkets(
      @RequestParam double minX,
      @RequestParam double minY,
      @RequestParam double maxX,
      @RequestParam double maxY,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {

    log.info(
        "[API] /api/markets called minX={}, minY={}, maxX={}, maxY={}, page={}, size={}",
        minX,
        minY,
        maxX,
        maxY,
        page,
        size);
    return service.getMarkers(minX, minY, maxX, maxY, page, size);
  }
}
