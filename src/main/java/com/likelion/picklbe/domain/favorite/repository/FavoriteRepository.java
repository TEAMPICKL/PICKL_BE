package com.likelion.picklbe.domain.favorite.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.favorite.entity.Favorite;
import com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  int countByUserIdAndType(Long userId, FavoriteType type);
}
