package com.likelion.picklbe.domain.quiz.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.quiz.entity.QuizDailyAttemptToken;
import com.likelion.picklbe.domain.quiz.repository.QuizDailyAttemptTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdRewardService {

  private final QuizDailyAttemptTokenRepository repo;

  @Transactional
  public int grantExtraAttempt(Long userId) {
    LocalDate today = LocalDate.now(); // 필요하면 Zone 지정

    var token =
        repo.findByUserIdAndDateForUpdate(userId, today)
            .orElseGet(() -> new QuizDailyAttemptToken(userId, today));

    // 날짜 넘어간 레코드면 일일 상태 리셋
    token.rolloverIfNewDay(today);

    // 하루 1회 제한
    token.grantFromAdOnce();

    // 신규 생성 케이스는 persist 필요
    if (token.getId() == null) {
      repo.save(token);
    }

    return token.getTokens(); // 남은(추가) 시도권 수 반환
  }
}
