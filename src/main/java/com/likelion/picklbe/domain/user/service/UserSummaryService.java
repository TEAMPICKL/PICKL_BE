package com.likelion.picklbe.domain.user.service;

import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType;
import com.likelion.picklbe.domain.favorite.repository.FavoriteRepository;
import com.likelion.picklbe.domain.history.repository.PickleHistoryRepository;
import com.likelion.picklbe.domain.point.entity.PointWallet;
import com.likelion.picklbe.domain.point.repository.PointWalletRepository;
import com.likelion.picklbe.domain.user.dto.response.UserSummaryResponse;
import com.likelion.picklbe.domain.user.entity.User;
import com.likelion.picklbe.domain.user.repository.UserRepository;
import com.likelion.picklbe.domain.user.support.FriendDays;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSummaryService {

  private final UserRepository userRepository;
  private final FavoriteRepository favoriteRepository;
  private final PickleHistoryRepository historyRepository;
  private final PointWalletRepository pointWalletRepository; // ← 추가

  @Transactional(readOnly = true)
  public UserSummaryResponse get(Long userId) {
    User u = userRepository.findById(userId).orElseThrow();

    long days = FriendDays.sinceInclusive(u.getCreatedAt().atZone(ZoneId.of("UTC")).toInstant());

    // 잔액은 지갑에서 직접 조회
    long points = pointWalletRepository.findById(userId).map(PointWallet::getBalance).orElse(0L);

    int favIng = favoriteRepository.countByUserIdAndType(userId, FavoriteType.INGREDIENT);
    int favRec = favoriteRepository.countByUserIdAndType(userId, FavoriteType.RECIPE);
    int hist = historyRepository.countByUserId(userId);

    return UserSummaryResponse.builder()
        .nickname(u.getNickname() != null ? u.getNickname() : u.getUsername())
        .region(null) // 실제 필드에 맞게 치환
        .points(points) // ← 지갑 잔액 기준
        .daysSinceFriend(days)
        .favoriteIngredientCount(favIng)
        .favoriteRecipeCount(favRec)
        .pickleHistoryCount(hist)
        .build();
  }
}
