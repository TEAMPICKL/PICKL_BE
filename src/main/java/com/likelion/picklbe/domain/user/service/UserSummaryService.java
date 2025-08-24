package com.likelion.picklbe.domain.user.service;

import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.favorite.entity.Favorite.FavoriteType;
import com.likelion.picklbe.domain.favorite.repository.FavoriteRepository;
import com.likelion.picklbe.domain.history.facade.PickleHistoryFacade;
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
  private final PickleHistoryFacade historyRepository;
  private final PointWalletRepository pointWalletRepository;

  @Transactional(readOnly = true)
  public UserSummaryResponse get(Long userId) {
    User u = userRepository.findById(userId).orElseThrow();

    long days = FriendDays.sinceInclusive(u.getCreatedAt().atZone(ZoneId.of("UTC")).toInstant());

    // 지갑 잔액
    long points = pointWalletRepository.findById(userId).map(PointWallet::getBalance).orElse(0L);

    // favorite 카운트: repository는 long을 반환하므로 안전 캐스팅
    long favIngLong = favoriteRepository.countByUserIdAndType(userId, FavoriteType.INGREDIENT);
    long favRecLong = favoriteRepository.countByUserIdAndType(userId, FavoriteType.RECIPE);
    int favIng = Math.toIntExact(favIngLong);
    int favRec = Math.toIntExact(favRecLong);

    int hist = historyRepository.countByUserId(userId);

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
