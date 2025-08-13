package com.likelion.picklbe.domain.user.service;

import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType;
import com.likelion.picklbe.domain.favorite.repository.FavoriteRepository;
import com.likelion.picklbe.domain.history.repository.PickleHistoryRepository;
import com.likelion.picklbe.domain.point.repository.PointRepository;
import com.likelion.picklbe.domain.user.dto.response.UserSummaryResponse;
import com.likelion.picklbe.domain.user.entity.User;
import com.likelion.picklbe.domain.user.repository.UserRepository;
import com.likelion.picklbe.domain.user.support.FriendDays;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSummaryService {

  private final UserRepository userRepository;
  private final FavoriteRepository favoriteRepository; // 네 프로젝트의 실제 레포로 바꿔줘
  private final PointRepository pointRepository; // 동일
  private final PickleHistoryRepository historyRepository;

  @Transactional(readOnly = true)
  public UserSummaryResponse get(Long userId) {
    User u = userRepository.findById(userId).orElseThrow();

    long days = FriendDays.sinceInclusive(u.getCreatedAt().atZone(ZoneId.of("UTC")).toInstant());
    long points = pointRepository.sumByUserId(userId);
    int favIng = favoriteRepository.countByUserIdAndType(userId, FavoriteType.INGREDIENT);
    int favRec = favoriteRepository.countByUserIdAndType(userId, FavoriteType.RECIPE);
    int hist = historyRepository.countByUserId(userId);
    // region/nickname은 실제 필드에 맞게 치환
    return UserSummaryResponse.builder()
        .nickname(u.getNickname() != null ? u.getNickname() : u.getUsername())
        .region(null)
        .points(points)
        .daysSinceFriend(days)
        .favoriteIngredientCount(favIng)
        .favoriteRecipeCount(favRec)
        .pickleHistoryCount(hist)
        .build();
  }
}
