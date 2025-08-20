package com.likelion.picklbe.domain.favorite.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.favorite.dto.IngredientCardDto;
import com.likelion.picklbe.domain.favorite.dto.RecipeCardDto;
import com.likelion.picklbe.domain.favorite.dto.response.FavoriteStatusResponse;
import com.likelion.picklbe.domain.favorite.entity.Favorite;
import com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType;
import com.likelion.picklbe.domain.favorite.repository.FavoriteRepository;
import com.likelion.picklbe.domain.user.entity.User;
import com.likelion.picklbe.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final UserRepository userRepository;

  /* 상태 조회 */
  @Transactional(readOnly = true)
  public FavoriteStatusResponse status(Long userId, FavoriteType type, Long targetId) {
    return favoriteRepository
        .findByUserIdAndTypeAndTargetId(userId, type, targetId)
        .map(f -> new FavoriteStatusResponse(true, f.getCreatedAt()))
        .orElseGet(() -> new FavoriteStatusResponse(false, null));
  }

  /* 등록 */
  @Transactional
  public FavoriteStatusResponse like(Long userId, FavoriteType type, Long targetId) {
    if (favoriteRepository.existsByUserIdAndTypeAndTargetId(userId, type, targetId)) {
      return status(userId, type, targetId);
    }
    User user = userRepository.getReferenceById(userId);
    Favorite saved =
        favoriteRepository.save(
            Favorite.builder().user(user).type(type).targetId(targetId).build());
    return new FavoriteStatusResponse(true, saved.getCreatedAt());
  }

  /* 해제 */
  @Transactional
  public FavoriteStatusResponse unlike(Long userId, FavoriteType type, Long targetId) {
    favoriteRepository.deleteByUserIdAndTypeAndTargetId(userId, type, targetId);
    return new FavoriteStatusResponse(false, null);
  }

  /* 토글 */
  @Transactional
  public FavoriteStatusResponse toggle(Long userId, FavoriteType type, Long targetId) {
    return favoriteRepository.existsByUserIdAndTypeAndTargetId(userId, type, targetId)
        ? unlike(userId, type, targetId)
        : like(userId, type, targetId);
  }

  /* 목록 조회 - 레포지토리에서 DTO로 바로 반환 */
  @Transactional(readOnly = true)
  public Page<RecipeCardDto> listFavoriteRecipes(Long userId, Pageable pageable) {
    return favoriteRepository.findRecipeCards(userId, FavoriteType.RECIPE, pageable);
  }

  @Transactional(readOnly = true)
  public Page<IngredientCardDto> listFavoriteIngredients(Long userId, Pageable pageable) {
    return favoriteRepository.findIngredientCards(userId, FavoriteType.INGREDIENT, pageable);
  }
}
