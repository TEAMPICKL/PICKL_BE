package com.likelion.picklbe.domain.seasonitems.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.likelion.picklbe.domain.seasonitems.dto.request.SeasonItemCreateRequest;
import com.likelion.picklbe.domain.seasonitems.dto.request.SeasonItemUpdateRequest;
import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemDetailDto;
import com.likelion.picklbe.domain.seasonitems.dto.response.SeasonItemSummaryDto;
import com.likelion.picklbe.domain.seasonitems.dto.seed.SeedPreviewDto;
import com.likelion.picklbe.domain.seasonitems.dto.seed.SeedRecipePreviewDto;
import com.likelion.picklbe.domain.seasonitems.dto.seed.SeedSeasonItemRequest;
import com.likelion.picklbe.domain.seasonitems.dto.seed.SeedSeasonItemResponse;
import com.likelion.picklbe.domain.seasonitems.entity.SeasonItem;
import com.likelion.picklbe.domain.seasonitems.exception.SeasonItemErrorCode;
import com.likelion.picklbe.domain.seasonitems.mapper.SeasonItemMapper;
import com.likelion.picklbe.domain.seasonitems.recipe.entity.Recipe;
import com.likelion.picklbe.domain.seasonitems.recipe.repository.RecipeRepository;
import com.likelion.picklbe.domain.seasonitems.repository.SeasonItemRepository;
import com.likelion.picklbe.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SeasonItemService {

  private final SeasonItemRepository seasonItemRepository;
  private final SeasonItemMapper seasonItemMapper;
  // ★ 추가
  private final RecipeRepository recipeRepository;
  private final WebClient langchainWebClient;

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
            .price(req.getPrice())
            .build();

    return seasonItemMapper.toDetailDto(seasonItemRepository.save(entity));
  }

  @Transactional
  public SeasonItemDetailDto update(Long id, SeasonItemUpdateRequest req) {
    SeasonItem cur = getEntity(id);
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
            .recommendedRecipes(cur.getRecommendedRecipes())
            .build();

    return seasonItemMapper.toDetailDto(seasonItemRepository.save(updated));
  }

  @Transactional
  public void delete(Long id) {
    SeasonItem cur = getEntity(id);
    seasonItemRepository.delete(cur);
  }

  // ========= ★ 여기부터: LLM 기반 자동 생성(seed) =========

  // Python(/seed/season-item) 요청/응답 형식 매핑
  private record PySeedReq(String itemname, int in_season_month, String mode, boolean dry_run) {}

  private record PyRecipe(
      String recipe_name,
      String ingredients,
      String instructions,
      String tip,
      String cooking_time_text,
      String recommend_tags_csv) {}

  private record PyPreview(
      String short_description,
      String representative_nutrient,
      String how_to_choose,
      String how_to_store,
      String how_to_trim,
      List<PyRecipe> recipes) {}

  private record PySeedRes(
      Long season_item_id, String upserted, Integer recipes_inserted, PyPreview preview) {}

  // 숫자 앞 공백 규칙: 한글/영문/닫는 괄호 바로 뒤에 숫자가 오면 공백 1개 삽입
  private static String spaceBeforeNumbers(String s) {
    if (s == null || s.isBlank()) {
      return s;
    }
    return s.replaceAll("(?<=[가-힣A-Za-z\\)])(?=\\d)", " ");
  }

  private SeedPreviewDto toPreview(PyPreview p) {
    List<SeedRecipePreviewDto> list = new ArrayList<>();
    if (p.recipes() != null) {
      for (PyRecipe r : p.recipes()) {
        list.add(
            SeedRecipePreviewDto.builder()
                .recipeName(spaceBeforeNumbers(r.recipe_name()))
                .ingredients(spaceBeforeNumbers(r.ingredients()))
                .instructions(spaceBeforeNumbers(r.instructions()))
                .tip(spaceBeforeNumbers(r.tip()))
                .cookingTimeText(spaceBeforeNumbers(r.cooking_time_text()))
                .recommendTagsCsv(spaceBeforeNumbers(r.recommend_tags_csv()))
                .build());
      }
    }
    return SeedPreviewDto.builder()
        .shortDescription(spaceBeforeNumbers(p.short_description()))
        .representativeNutrient(spaceBeforeNumbers(p.representative_nutrient()))
        .howToChoose(spaceBeforeNumbers(p.how_to_choose()))
        .howToStore(spaceBeforeNumbers(p.how_to_store()))
        .howToTrim(spaceBeforeNumbers(p.how_to_trim()))
        .recipes(list)
        .build();
  }

  /**
   * LLM으로 초안 생성 → (옵션) 저장.
   *
   * @param req itemname + seasonMonth, replace, dryRun
   * @param authHeader 스프링 JWT 그대로 전달 (langchain에서 사용자 인증/감사 로그 용)
   */
  @Transactional
  public SeedSeasonItemResponse seedFromLLM(SeedSeasonItemRequest req, String authHeader) {
    String mode = Boolean.TRUE.equals(req.getReplace()) ? "replace" : "append";

    // 1) Python(langchain) 서비스에 dry-run 호출로 초안 생성
    PySeedRes py =
        langchainWebClient
            .post()
            .uri("/seed/season-item")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .bodyValue(new PySeedReq(req.getItemname(), req.getSeasonMonth(), mode, true))
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                rsp ->
                    rsp.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("LLM 생성 실패: " + body))))
            .bodyToMono(PySeedRes.class)
            .block();

    if (py == null || py.preview() == null) {
      throw new CustomException(SeasonItemErrorCode.SEASON_ITEM_NOT_FOUND);
    }

    // 2) 미리보기 구성
    SeedPreviewDto preview = toPreview(py.preview());

    // dryRun=true면 저장하지 않고 미리보기만 반환
    if (Boolean.TRUE.equals(req.getDryRun())) {
      return SeedSeasonItemResponse.builder()
          .seasonItemId(null)
          .upserted(null)
          .recipesInserted(0)
          .preview(preview)
          .build();
    }

    // 3) DB 저장(스프링이 직접) — SeasonItem upsert
    SeasonItem item =
        seasonItemRepository
            .findByItemname(req.getItemname())
            .orElseGet(
                () ->
                    SeasonItem.builder()
                        .itemname(req.getItemname())
                        .inSeasonMonth(req.getSeasonMonth())
                        // NOT NULL 기본값들
                        .imageUrl("")
                        .tip("")
                        .calorie("")
                        .price(0)
                        .unit("")
                        .build());

    boolean isInsert = (item.getId() == null);

    item =
        SeasonItem.builder()
            .id(item.getId())
            .itemname(item.getItemname())
            .inSeasonMonth(req.getSeasonMonth())
            .shortDescription(preview.getShortDescription())
            .representativeNutrient(preview.getRepresentativeNutrient())
            .howToChoose(preview.getHowToChoose())
            .howToStore(preview.getHowToStore())
            .howToTrim(preview.getHowToTrim())
            // 유지 필드
            .imageUrl(item.getImageUrl() == null ? "" : item.getImageUrl())
            .tip(item.getTip() == null ? "" : item.getTip())
            .calorie(item.getCalorie() == null ? "" : item.getCalorie())
            .price(item.getPrice() == null ? 0 : item.getPrice())
            .unit(item.getUnit() == null ? "" : item.getUnit())
            .recommendedRecipes(item.getRecommendedRecipes())
            .build();

    item = seasonItemRepository.save(item);

    // 4) 레시피 저장 (최대 2개)
    int inserted = 0;
    List<SeedRecipePreviewDto> recipes = preview.getRecipes();
    if (recipes != null && !recipes.isEmpty()) {
      if (Boolean.TRUE.equals(req.getReplace())) {
        recipeRepository.deleteBySeasonItemId(item.getId());
      }
      int limit = Math.min(2, recipes.size());
      for (int i = 0; i < limit; i++) {
        SeedRecipePreviewDto r = recipes.get(i);
        // 엔티티 필드명에 맞게 수정
        Recipe entity =
            Recipe.builder()
                .seasonItem(item)
                .recipeName(r.getRecipeName())
                .ingredients(r.getIngredients())
                .instructions(r.getInstructions())
                .tip(r.getTip())
                .cookingTimeText(r.getCookingTimeText())
                .recommendTagsCsv(r.getRecommendTagsCsv())
                .build();
        recipeRepository.save(entity);
        inserted++;
      }
    }

    return SeedSeasonItemResponse.builder()
        .seasonItemId(item.getId())
        .upserted(isInsert ? "inserted" : "updated")
        .recipesInserted(inserted)
        .preview(preview)
        .build();
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
