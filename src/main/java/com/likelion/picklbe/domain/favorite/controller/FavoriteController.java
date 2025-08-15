package com.likelion.picklbe.domain.favorite.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.likelion.picklbe.domain.favorite.dto.IngredientCardDto;
import com.likelion.picklbe.domain.favorite.dto.RecipeCardDto;
import com.likelion.picklbe.domain.favorite.dto.request.FavoriteToggleRequest;
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
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final FavoriteRepository favoriteRepository;

  @Operation(
      summary = "찜 상태 조회",
      description = "특정 대상(FavoriteType, targetId)에 대해 현재 사용자가 찜했는지 여부를 반환합니다.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "조회 성공",
              content = @Content(schema = @Schema(implementation = FavoriteStatusResponse.class)))
      })
  @GetMapping("/status")
  public BaseResponse<FavoriteStatusResponse> status(
      @AuthenticationPrincipal CustomUserDetails me,
      @Parameter(description = "찜 대상 타입 (예: INGREDIENT, RECIPE)", required = true) @RequestParam
      FavoriteType type,
      @Parameter(description = "찜 대상 ID", required = true) @RequestParam Long targetId) {
    return BaseResponse.success("상태 조회 성공", favoriteService.status(me.getId(), type, targetId));
  }

  @Operation(
      summary = "찜 등록",
      description = "특정 대상에 대해 찜을 등록합니다.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "등록 성공",
              content = @Content(schema = @Schema(implementation = FavoriteStatusResponse.class)))
      })
  @PostMapping
  public BaseResponse<FavoriteStatusResponse> like(
      @AuthenticationPrincipal CustomUserDetails me,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "찜 등록 요청 바디 (대상 타입, 대상 ID)",
          required = true,
          content = @Content(schema = @Schema(implementation = FavoriteToggleRequest.class)))
      @RequestBody
      FavoriteToggleRequest req) {
    return BaseResponse.success(
        "찜 등록 성공", favoriteService.like(me.getId(), req.type(), req.targetId()));
  }

  @Operation(
      summary = "찜 해제",
      description = "특정 대상에 대해 찜을 해제합니다.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "해제 성공",
              content = @Content(schema = @Schema(implementation = FavoriteStatusResponse.class)))
      })
  @DeleteMapping
  public BaseResponse<FavoriteStatusResponse> unlike(
      @AuthenticationPrincipal CustomUserDetails me,
      @Parameter(description = "찜 대상 타입 (예: INGREDIENT, RECIPE)", required = true) @RequestParam
      FavoriteType type,
      @Parameter(description = "찜 대상 ID", required = true) @RequestParam Long targetId) {
    return BaseResponse.success("찜 해제 성공", favoriteService.unlike(me.getId(), type, targetId));
  }

  @Operation(
      summary = "찜한 식재료 목록 조회",
      description = "현재 사용자가 찜한 식재료 목록을 페이지네이션 형태로 반환합니다.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "조회 성공",
              content = @Content(schema = @Schema(implementation = IngredientCardDto.class)))
      })
  @GetMapping("/ingredients")
  public BaseResponse<Page<IngredientCardDto>> favoriteIngredients(
      @AuthenticationPrincipal CustomUserDetails me,
      @Parameter(
          description = "페이지네이션 정보 (예: page=0, size=20, sort=createdAt,desc)\n" +
              "기본: size=20, createdAt DESC"
      )
      @PageableDefault(size = 20)
      Pageable pageable) {
    return BaseResponse.success(
        "찜한 식재료 목록 조회 성공",
        favoriteRepository.findIngredientCards(me.getId(), FavoriteType.INGREDIENT, pageable));
  }

  @Operation(
      summary = "찜한 레시피 목록 조회",
      description = "현재 사용자가 찜한 레시피 목록을 페이지네이션 형태로 반환합니다.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "조회 성공",
              content = @Content(schema = @Schema(implementation = RecipeCardDto.class)))
      })
  @GetMapping("/recipes")
  public BaseResponse<Page<RecipeCardDto>> favoriteRecipes(
      @AuthenticationPrincipal CustomUserDetails me,
      @Parameter(
          description = "페이지네이션 정보 (예: page=0, size=20, sort=createdAt,desc)\n" +
              "기본: size=20, createdAt DESC"
      )
      @PageableDefault(size = 20)
      Pageable pageable) {
    return BaseResponse.success(
        "찜한 레시피 목록 조회 성공",
        favoriteRepository.findRecipeCards(me.getId(), FavoriteType.RECIPE, pageable));
  }
}
