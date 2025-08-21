package com.likelion.picklbe.domain.mart.controller;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.likelion.picklbe.domain.mart.dto.PlaceResponse;
import com.likelion.picklbe.domain.mart.service.MartQueryService;
import com.likelion.picklbe.global.response.BaseResponse;

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
@Tag(name = "Places", description = "대형마트/슈퍼마켓 조회 API (로컬 DB)")
@RequestMapping("/api/places")
public class MartController {

  private final MartQueryService martQueryService;

  // BBOX 파라미터 예: bounds=126.764,37.413,127.183,37.715 & center=127.0,37.55
  @Operation(
      summary = "현재 지도 BBOX 내 대형마트/슈퍼마켓 목록",
      description =
          """
          로컬 DB에서 대형마트/슈퍼마켓을 조회합니다.
          - 좌하단(minX, minY) ~ 우상단(maxX, maxY)의 BBOX로 조회
          - 좌표계: WGS84 (경도 lng, 위도 lat)
          - 너무 넓은 영역은 성능 보호를 위해 차단됩니다.
          """)
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = PlaceResponse.class))))
  @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터", content = @Content)
  @GetMapping
  public BaseResponse<List<PlaceResponse>> getByBounds(
      @Parameter(
              description = "BBOX: westLng,southLat,eastLng,northLat",
              schema = @Schema(example = "126.764,37.413,127.183,37.715"))
          @RequestParam
          String bounds,
      @Parameter(description = "지도 중심: lng,lat", schema = @Schema(example = "127.000,37.550"))
          @RequestParam
          String center,
      @Parameter(
              description = "최대 반환 개수(1~1000 권장)",
              schema =
                  @Schema(example = "300", defaultValue = "300", minimum = "1", maximum = "1000"))
          @RequestParam(defaultValue = "300")
          @Min(1)
          @Max(1000)
          int limit) {

    final double[] b = parse4(bounds, "bounds");
    final double[] c = parse2(center, "center");

    final double west = b[0], south = b[1], east = b[2], north = b[3];
    final double centerLng = c[0], centerLat = c[1];

    // 유효성 체크
    if (west >= east || south >= north) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "bounds가 올바르지 않습니다. west<east, south<north 이어야 합니다.");
    }

    // 지나치게 넓은 영역 차단(도 단위 면적, 필요 시 조절)
    double area = Math.abs(east - west) * Math.abs(north - south);
    if (area > 0.30) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색 범위가 너무 넓어요. 지도를 더 확대해 주세요.");
    }

    log.info("[API] /api/places bounds={}, center={}, limit={}", bounds, center, limit);

    var data = martQueryService.findInBounds(west, south, east, north, centerLng, centerLat, limit);
    return BaseResponse.success("장소 조회 성공", data);
  }

  // 반경 검색: /api/places/near?lng=127&lat=37.5&radius=1500&limit=200
  @Operation(
      summary = "반경 내 대형마트/슈퍼마켓 목록",
      description =
          """
          로컬 DB에서 중심 좌표(lng,lat) 기준 반경(radius m) 내 결과를 조회합니다.
          - 좌표계: WGS84 (경도 lng, 위도 lat)
          """)
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = PlaceResponse.class))))
  @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터", content = @Content)
  @GetMapping("/near")
  public BaseResponse<List<PlaceResponse>> getNearby(
      @Parameter(description = "경도(lng)", schema = @Schema(example = "127.000")) @RequestParam
          double lng,
      @Parameter(description = "위도(lat)", schema = @Schema(example = "37.550")) @RequestParam
          double lat,
      @Parameter(
              description = "반경(m)",
              schema =
                  @Schema(
                      example = "1500",
                      defaultValue = "1500",
                      minimum = "100",
                      maximum = "5000"))
          @RequestParam(defaultValue = "1500")
          @Min(100)
          @Max(5000)
          int radius,
      @Parameter(
              description = "최대 반환 개수(1~1000 권장)",
              schema =
                  @Schema(example = "200", defaultValue = "200", minimum = "1", maximum = "1000"))
          @RequestParam(defaultValue = "200")
          @Min(1)
          @Max(1000)
          int limit) {

    log.info("[API] /api/places/near lng={}, lat={}, radius={}, limit={}", lng, lat, radius, limit);

    var data = martQueryService.findNearby(lng, lat, radius, limit);
    return BaseResponse.success("반경 조회 성공", data);
  }

  // ------------ utils ------------
  private double[] parse4(String csv, String name) {
    try {
      String[] t = csv.split(",");
      if (t.length != 4) {
        throw new IllegalArgumentException();
      }
      return new double[] {
        Double.parseDouble(t[0]), Double.parseDouble(t[1]),
        Double.parseDouble(t[2]), Double.parseDouble(t[3])
      };
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, name + " 파라미터는 'west,south,east,north' 형식이어야 합니다.", e);
    }
  }

  private double[] parse2(String csv, String name) {
    try {
      String[] t = csv.split(",");
      if (t.length != 2) {
        throw new IllegalArgumentException();
      }
      return new double[] {Double.parseDouble(t[0]), Double.parseDouble(t[1])};
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, name + " 파라미터는 'lng,lat' 형식이어야 합니다.", e);
    }
  }
}
