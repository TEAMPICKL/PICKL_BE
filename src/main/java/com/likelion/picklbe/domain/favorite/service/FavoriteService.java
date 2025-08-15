package com.likelion.picklbe.domain.favorite.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional(readOnly = true)
  public FavoriteStatusResponse status(Long userId, FavoriteType type, Long targetId) {
    return favoriteRepository
        .findByUserIdAndTypeAndTargetId(userId, type, targetId)
        .map(f -> new FavoriteStatusResponse(true, f.getCreatedAt()))
        .orElseGet(() -> new FavoriteStatusResponse(false, null));
  }

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

  @Transactional
  public FavoriteStatusResponse unlike(Long userId, FavoriteType type, Long targetId) {
    favoriteRepository.deleteByUserIdAndTypeAndTargetId(userId, type, targetId);
    return new FavoriteStatusResponse(false, null);
  }

  @Transactional
  public FavoriteStatusResponse toggle(Long userId, FavoriteType type, Long targetId) {
    return favoriteRepository.existsByUserIdAndTypeAndTargetId(userId, type, targetId)
        ? unlike(userId, type, targetId)
        : like(userId, type, targetId);
  }
}
