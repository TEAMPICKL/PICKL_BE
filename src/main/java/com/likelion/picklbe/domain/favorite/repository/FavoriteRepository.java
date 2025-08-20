package com.likelion.picklbe.domain.favorite.repository;

import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.likelion.picklbe.domain.favorite.dto.IngredientCardDto;
import com.likelion.picklbe.domain.favorite.dto.RecipeCardDto;
import com.likelion.picklbe.domain.favorite.entity.Favorite;
import com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  long countByUserIdAndType(Long userId, FavoriteType type);

  // 편의 헬퍼
  default long countIngredients(Long userId) {
    return countByUserIdAndType(userId, FavoriteType.INGREDIENT);
  }

  default long countRecipes(Long userId) {
    return countByUserIdAndType(userId, FavoriteType.RECIPE);
  }

  boolean existsByUserIdAndTypeAndTargetId(Long userId, FavoriteType type, Long targetId);

  Optional<Favorite> findByUserIdAndTypeAndTargetId(Long userId, FavoriteType type, Long targetId);

  void deleteByUserIdAndTypeAndTargetId(Long userId, FavoriteType type, Long targetId);

  /* =========================
   * 레시피 찜 목록 (season_item_recipe)
   * ========================= */
  @Query(
      value =
          """
                select
                  r.id           as id,
                  r.recipe_name  as recipeName,
                  f.created_at   as likedAt
                from favorites f
                join season_item_recipe r on r.id = f.target_id
                where f.user_id = :userId
                  and f.type = 'RECIPE'
                order by f.created_at desc
              """,
      countQuery =
          """
                select count(*)
                from favorites f
                where f.user_id = :userId
                  and f.type = 'RECIPE'
              """,
      nativeQuery = true)
  Page<RecipeCardRow> findRecipeCardRows(@Param("userId") Long userId, Pageable pageable);

  interface RecipeCardRow {

    Long getId();

    String getRecipeName();

    Timestamp getLikedAt();
  }

  /** 컨트롤러 시그니처와 동일한 메서드 (Row -> DTO 매핑) */
  default Page<RecipeCardDto> findRecipeCards(Long userId, FavoriteType type, Pageable pageable) {
    // 방어적으로 타입 확인 (컨트롤러에서 RECIPE로만 호출됨)
    if (type != FavoriteType.RECIPE) {
      throw new IllegalArgumentException("findRecipeCards: type must be RECIPE");
    }
    return findRecipeCardRows(userId, pageable)
        .map(
            r ->
                new RecipeCardDto(
                    r.getId(),
                    r.getRecipeName(),
                    r.getLikedAt() != null ? r.getLikedAt().toLocalDateTime() : null));
  }

  /* =========================
   * 식재료 찜 목록 (kamis_item_price 최신행)
   * 이미지: DB의 image_url 그대로 사용
   * ========================= */

  // kamis_item_price에서 product_no별 최신행을 뽑아 조인 (동일 날짜 다건 대비)
  @Query(
      value =
          """
                SELECT
                  kip.product_no    AS productNo,
                  kip.product_name  AS productName,
                  kip.image_url     AS imageUrl,
                  f.created_at      AS likedAt
                FROM favorites f
                JOIN (
                  SELECT k.*
                  FROM kamis_item_price k
                  JOIN (
                    SELECT product_no, MAX(price_date) AS max_date
                    FROM kamis_item_price
                    GROUP BY product_no
                  ) mx
                    ON mx.product_no = k.product_no
                   AND mx.max_date  = k.price_date
                  /* 동일 날짜 다건 대비 (선택) */
                  JOIN (
                    SELECT product_no, price_date, MAX(id) AS max_id
                    FROM kamis_item_price
                    GROUP BY product_no, price_date
                  ) mi
                    ON mi.product_no = k.product_no
                   AND mi.price_date = k.price_date
                   AND mi.max_id     = k.id
                ) kip
                  ON kip.product_no = CAST(f.target_id AS UNSIGNED)
                WHERE f.user_id = :userId
                  AND f.type = 'INGREDIENT'
                ORDER BY f.created_at DESC
              """,
      countQuery =
          """
                SELECT COUNT(*)
                FROM favorites f
                WHERE f.user_id = :userId
                  AND f.type = 'INGREDIENT'
              """,
      nativeQuery = true)
  Page<IngredientCardRow> findIngredientCardRows(@Param("userId") Long userId, Pageable pageable);

  interface IngredientCardRow {

    Long getProductNo();

    String getProductName();

    String getImageUrl();

    Timestamp getLikedAt();
  }

  /** 컨트롤러 시그니처와 동일한 메서드 (Row -> DTO 매핑) */
  default Page<IngredientCardDto> findIngredientCards(
      Long userId, FavoriteType type, Pageable pageable) {
    if (type != FavoriteType.INGREDIENT) {
      throw new IllegalArgumentException("findIngredientCards: type must be INGREDIENT");
    }
    return findIngredientCardRows(userId, pageable)
        .map(
            r ->
                new IngredientCardDto(
                    r.getProductNo(), // id
                    r.getProductName(), // name
                    r.getImageUrl(), // image url (썸네일)
                    null, // shortDesc는 원격데이터에 없으므로 사용 안함
                    r.getLikedAt() != null ? r.getLikedAt().toLocalDateTime() : null));
  }
}
