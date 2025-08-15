package com.likelion.picklbe.domain.favorite.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.favorite.dto.IngredientCardDto;
import com.likelion.picklbe.domain.favorite.entity.Favorite;
import com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  int countByUserIdAndType(Long userId, FavoriteType type);

  boolean existsByUserIdAndTypeAndTargetId(Long userId, FavoriteType type, Long targetId);

  Optional<Favorite> findByUserIdAndTypeAndTargetId(Long userId, FavoriteType type, Long targetId);

  @Transactional
  void deleteByUserIdAndTypeAndTargetId(Long userId, FavoriteType type, Long targetId);

  // --- 목록용 프로젝션 (식재료/레시피 카드) ---
  @Query(
      """
      select new com.likelion.picklbe.domain.favorite.dto.IngredientCardDto(
        i.id, i.name, i.thumbnailUrl, i.shortDesc, f.createdAt
      )
      from Favorite f
      join Ingredient i on i.id = f.targetId
      where f.user.id = :userId and f.type = com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType.INGREDIENT
      """)
  Page<IngredientCardDto> findIngredientCards(Long userId, Pageable pageable);

  @Query(
      """
      select new com.likelion.picklbe.domain.favorite.dto.RecipeCardDto(
        r.id, r.title, r.thumbnailUrl, f.createdAt
      )
      from Favorite f
      join Recipe r on r.id = f.targetId
      where f.user.id = :userId and f.type = com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType.RECIPE
      """)
  Page<com.likelion.picklbe.domain.favorite.dto.RecipeCardDto> findRecipeCards(
      Long userId, Pageable pageable);
}
