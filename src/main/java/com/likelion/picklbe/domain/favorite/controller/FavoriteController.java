package com.likelion.picklbe.domain.favorite.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.likelion.picklbe.domain.favorite.dto.IngredientCardDto;
import com.likelion.picklbe.domain.favorite.dto.RecipeCardDto;
import com.likelion.picklbe.domain.favorite.dto.request.FavoriteToggleRequest;
import com.likelion.picklbe.domain.favorite.dto.response.FavoriteCountsResponse;
import com.likelion.picklbe.domain.favorite.dto.response.FavoriteStatusResponse;
import com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType;
import com.likelion.picklbe.domain.favorite.repository.FavoriteRepository;
import com.likelion.picklbe.domain.favorite.service.FavoriteService;
import com.likelion.picklbe.global.response.BaseResponse;
import com.likelion.picklbe.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "favorite-controller", description = "찜 등록/해제/조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final FavoriteRepository favoriteRepository;

  // ---------- 단건 상태 조회 ----------
  @Operation(
      summary = "찜 상태 조회",
      description =
          """
              특정 대상이 현재 사용자에게 '찜'되어 있는지 확인합니다.
              - 쿼리: type(INGREDIENT|RECIPE), targetId
              - 응답: isLiked, likedAt
              """,
      responses =
          @ApiResponse(
              responseCode = "200",
              description = "조회 성공",
              content = @Content(schema = @Schema(implementation = FavoriteStatusResponse.class))))
  @GetMapping("/status")
  public BaseResponse<FavoriteStatusResponse> status(
      @AuthenticationPrincipal CustomUserDetails me,
      @Parameter(description = "찜 대상 타입 (INGREDIENT | RECIPE)", required = true) @RequestParam
          FavoriteType type,
      @Parameter(description = "찜 대상 ID", required = true) @RequestParam Long targetId) {
    return BaseResponse.success("상태 조회 성공", favoriteService.status(me.getId(), type, targetId));
  }

  // ---------- 등록/해제/토글 : 바디형 ----------
  @Operation(
      summary = "찜 등록 (JSON 바디)",
      description =
          """
              대상 타입과 ID를 JSON 바디로 보내 '찜 등록'합니다.
              예시 바디:
              {
                "type": "INGREDIENT",
                "targetId": 339
              }
              """,
      responses =
          @ApiResponse(
              responseCode = "200",
              description = "등록 성공",
              content = @Content(schema = @Schema(implementation = FavoriteStatusResponse.class))))
  @PostMapping
  public BaseResponse<FavoriteStatusResponse> like(
      @AuthenticationPrincipal CustomUserDetails me,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = FavoriteToggleRequest.class)))
          @RequestBody
          FavoriteToggleRequest req) {
    return BaseResponse.success(
        "찜 등록 성공", favoriteService.like(me.getId(), req.type(), req.targetId()));
  }

  @Operation(
      summary = "찜 해제 (쿼리스트링)",
      description = "type, targetId를 쿼리로 보내 '찜 해제'합니다.",
      responses =
          @ApiResponse(
              responseCode = "200",
              description = "해제 성공",
              content = @Content(schema = @Schema(implementation = FavoriteStatusResponse.class))))
  @DeleteMapping
  public BaseResponse<FavoriteStatusResponse> unlike(
      @AuthenticationPrincipal CustomUserDetails me,
      @RequestParam FavoriteType type,
      @RequestParam Long targetId) {
    return BaseResponse.success("찜 해제 성공", favoriteService.unlike(me.getId(), type, targetId));
  }

  @Operation(
      summary = "찜 토글 (JSON 바디)",
      description = "이미 찜이면 해제, 아니면 등록합니다. 바디 형식은 등록과 동일합니다.",
      responses =
          @ApiResponse(
              responseCode = "200",
              description = "토글 성공",
              content = @Content(schema = @Schema(implementation = FavoriteStatusResponse.class))))
  @PostMapping("/toggle")
  public BaseResponse<FavoriteStatusResponse> toggle(
      @AuthenticationPrincipal CustomUserDetails me,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = FavoriteToggleRequest.class)))
          @RequestBody
          FavoriteToggleRequest req) {
    return BaseResponse.success(
        "찜 토글 성공", favoriteService.toggle(me.getId(), req.type(), req.targetId()));
  }

  // ---------- 목록 조회 (Pageable 기본값 적용) ----------
  @Operation(
      summary = "찜한 식재료 목록 조회",
      description =
          """
              아무 파라미터 없이 호출해도 됩니다.
              - 기본값: page=0, size=20
              - 정렬: 서버 고정(createdAt DESC)
              """)
  @GetMapping("/ingredients")
  public BaseResponse<Page<IngredientCardDto>> favoriteIngredients(
      @AuthenticationPrincipal CustomUserDetails me,
      @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {

    Pageable safe = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    return BaseResponse.success(
        "찜한 식재료 목록 조회 성공", favoriteService.listFavoriteIngredients(me.getId(), safe));
  }

  @Operation(
      summary = "찜한 레시피 목록 조회",
      description =
          """
              아무 파라미터 없이 호출해도 됩니다.
              - 기본값: page=0, size=20
              - 정렬: 서버 고정(createdAt DESC)
              """)
  @GetMapping("/recipes")
  public BaseResponse<Page<RecipeCardDto>> favoriteRecipes(
      @AuthenticationPrincipal CustomUserDetails me,
      @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {

    Pageable safe = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    return BaseResponse.success(
        "찜한 레시피 목록 조회 성공", favoriteService.listFavoriteRecipes(me.getId(), safe));
  }

  // ---------- 개수 조회 ----------
  @Operation(
      summary = "찜 개수 조회",
      description = "현재 사용자의 찜 개수(식재료, 레시피)를 한 번에 반환합니다.",
      responses =
          @ApiResponse(
              responseCode = "200",
              description = "조회 성공",
              content = @Content(schema = @Schema(implementation = FavoriteCountsResponse.class))))
  @GetMapping("/counts")
  public BaseResponse<FavoriteCountsResponse> favoriteCounts(
      @AuthenticationPrincipal CustomUserDetails me) {
    return BaseResponse.success("찜 개수 조회 성공", favoriteService.counts(me.getId()));
  }
}
