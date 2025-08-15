package com.likelion.picklbe.domain.point.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.point.entity.PointTx;
import com.likelion.picklbe.domain.point.entity.PointWallet;
import com.likelion.picklbe.domain.point.repository.PointTxRepository;
import com.likelion.picklbe.domain.point.repository.PointWalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

  private final PointWalletRepository walletRepo;
  private final PointTxRepository txRepo;

  /** 지갑 없으면 0원으로 생성 후 반환 */
  @Transactional
  public PointWallet getOrCreateWallet(Long userId) {
    return walletRepo
        .findById(userId)
        .orElseGet(
            () -> {
              PointWallet w = new PointWallet();
              w.setUserId(userId);
              w.setBalance(30000L);
              return walletRepo.save(w);
            });
  }

  /** 현재 잔액 조회(없으면 0) */
  @Transactional(readOnly = true)
  public long getBalance(Long userId) {
    return walletRepo.findById(userId).map(PointWallet::getBalance).orElse(0L);
  }

  /** 내역 페이지 조회 */
  @Transactional(readOnly = true)
  public Page<PointTx> getHistory(Long userId, Pageable pageable) {
    return txRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  /**
   * 포인트 적립(일반용) - 지갑 자동 생성 - 적립 트랜잭션 저장
   *
   * @return 적립 후 잔액
   */
  @Transactional
  public long addPoints(Long userId, long amount, String reason, Long refId) {
    // 지갑
    PointWallet wallet = getOrCreateWallet(userId);
    wallet.setBalance(wallet.getBalance() + amount);
    walletRepo.save(wallet);

    // 트랜잭션 기록
    PointTx tx = new PointTx();
    tx.setUserId(userId);
    tx.setAmount(amount);
    tx.setReason(reason);
    tx.setRefId(refId);
    tx.setCreatedAt(LocalDateTime.now());
    txRepo.save(tx);

    return wallet.getBalance();
  }

  /**
   * 일일 퀴즈 보상 적립 (idempotent by reason+refId) - 동일 userId/QUIZ_DAILY/dailyQuizId 조합이 존재하면 적립하지 않음
   *
   * @return 실제 적립했으면 true, 이미 적립되어 있으면 false
   */
  @Transactional
  public boolean earnDailyQuizOnce(Long userId, long amount, Long dailyQuizId) {
    // 이미 적립했으면 false
    if (txRepo.existsByUserIdAndReasonAndRefId(userId, "QUIZ_DAILY", dailyQuizId)) {
      return false;
    }

    // 트랜잭션 기록
    PointTx tx = new PointTx();
    tx.setUserId(userId);
    tx.setAmount(amount);
    tx.setReason("QUIZ_DAILY");
    tx.setRefId(dailyQuizId);
    tx.setCreatedAt(LocalDateTime.now());
    txRepo.save(tx);

    // 지갑 반영
    PointWallet wallet = getOrCreateWallet(userId);
    wallet.setBalance(wallet.getBalance() + amount);
    walletRepo.save(wallet);

    return true;
  }
}
