package com.likelion.picklbe.domain.point.service;

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

  @Transactional
  public long addPoints(Long userId, long amount, String reason, Long refId) {
    PointWallet wallet =
        walletRepo
            .findById(userId)
            .orElseGet(
                () -> {
                  PointWallet w = new PointWallet();
                  w.setUserId(userId);
                  return walletRepo.save(w);
                });
    wallet.setBalance(wallet.getBalance() + amount);
    walletRepo.save(wallet);

    PointTx tx = new PointTx();
    tx.setUserId(userId);
    tx.setAmount(amount);
    tx.setReason(reason);
    tx.setRefId(refId);
    txRepo.save(tx);
    return wallet.getBalance();
  }
}
