package com.likelion.picklbe.domain.point.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

  private static final long DEFAULT_BALANCE = 30_000L;
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  /** 지갑 없으면 30000원으로 생성 후 반환 */
  @Transactional
  public PointWallet getOrCreateWallet(Long userId) {
    return walletRepo.findById(userId).orElseGet(() -> createWalletWithDefaultBalance(userId));
  }

  private PointWallet createWalletWithDefaultBalance(Long userId) {
    PointWallet wallet = new PointWallet();
    wallet.setUserId(userId);
    wallet.setBalance(DEFAULT_BALANCE);
    return walletRepo.save(wallet);
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
   * 일반 적립(무조건 적립) - 지갑 자동 생성, 트랜잭션 기록
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
    tx.setCreatedAt(LocalDateTime.now(KST)); // KST로 통일
    txRepo.save(tx);

    return wallet.getBalance();
  }

  /**
   * 일일 퀴즈 보상 적립 (중복 허용) - 맞출 때마다 적립 시도번호/날짜를 reason 메타로 함께 저장하고, refId에는 quizPoolId를 저장.
   *
   * @return 적립 후 잔액
   */
  @Transactional
  public long earnDailyQuiz(
      Long userId, long amount, Long quizPoolId, int attemptNo, LocalDate quizDate) {
    // reason에 메타 포함 (엔티티에 별도 필드가 없으므로 안전한 문자열 인코딩)
    String reasonWithMeta = String.format("QUIZ_DAILY[attempt=%d,date=%s]", attemptNo, quizDate);

    // 지갑
    PointWallet wallet = getOrCreateWallet(userId);
    wallet.setBalance(wallet.getBalance() + amount);
    walletRepo.save(wallet);

    // 트랜잭션 기록
    PointTx tx = new PointTx();
    tx.setUserId(userId);
    tx.setAmount(amount);
    tx.setReason(reasonWithMeta); // 메타 포함
    tx.setRefId(quizPoolId); // 퀴즈 식별자
    tx.setCreatedAt(LocalDateTime.now(KST)); // 기록 시각은 KST 현재
    txRepo.save(tx);

    return wallet.getBalance();
  }
}
