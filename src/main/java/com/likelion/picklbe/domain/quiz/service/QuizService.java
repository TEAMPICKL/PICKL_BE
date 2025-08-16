package com.likelion.picklbe.domain.quiz.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.point.service.PointService;
import com.likelion.picklbe.domain.quiz.entity.DailyQuiz;
import com.likelion.picklbe.domain.quiz.entity.QuizAttempt;
import com.likelion.picklbe.domain.quiz.entity.QuizPool;
import com.likelion.picklbe.domain.quiz.repository.DailyQuizRepository;
import com.likelion.picklbe.domain.quiz.repository.QuizAttemptRepository;
import com.likelion.picklbe.domain.quiz.repository.QuizPoolRepository;
import com.likelion.picklbe.global.exception.ApiException;
import com.likelion.picklbe.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizService {

  private final DailyQuizRepository dailyQuizRepo;
  private final QuizPoolRepository quizPoolRepo;
  private final QuizAttemptRepository attemptRepo;
  private final PointService pointService;

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final int N_DAYS_NO_REPEAT = 7;
  private static final int QUIZ_REWARD = 100;

  public DailyQuiz getOrThrowTodayQuiz() {
    LocalDate today = LocalDate.now(KST);
    return dailyQuizRepo
        .findByQuizDate(today)
        .orElseThrow(() -> ApiException.of(ErrorCode.QUIZ_NOT_READY));
  }

  @Transactional
  public DailyQuiz createTodayQuizIfAbsent() {
    LocalDate today = LocalDate.now(KST);
    return dailyQuizRepo
        .findByQuizDate(today)
        .orElseGet(
            () -> {
              // 최근 N일 내에 사용하지 않은 후보를 랜덤 픽
              LocalDate threshold = today.minusDays(N_DAYS_NO_REPEAT);
              List<QuizPool> candidates =
                  quizPoolRepo.findPickableRandom(threshold, PageRequest.of(0, 10));
              if (candidates.isEmpty()) {
                throw ApiException.of(ErrorCode.QUIZ_POOL_EMPTY);
              }

              QuizPool picked = candidates.get(0);
              picked.setLastUsedDate(today); // dirty checking 으로 반영됨 (@Transactional)

              DailyQuiz dq = new DailyQuiz();
              dq.setQuizDate(today);
              dq.setQuizPool(picked);
              dq.setIngredient(picked.getIngredient());
              dq.setStatement(picked.getStatement());
              dq.setAnswer(picked.getAnswer());
              return dailyQuizRepo.save(dq);
            });
  }

  @Transactional
  public AnswerResult answer(Long userId, boolean userAnswer, String idempotencyKey) {
    LocalDate today = LocalDate.now(KST);
    if (attemptRepo.existsByUserIdAndQuizDate(userId, today)) {
      throw ApiException.of(ErrorCode.ALREADY_ATTEMPTED);
    }
    DailyQuiz quiz = getOrThrowTodayQuiz();

    boolean correct = (quiz.getAnswer().booleanValue() == userAnswer);

    int awarded = 0;
    Long walletBalance = null;

    if (correct) {
      boolean firstTime = pointService.earnDailyQuizOnce(userId, (long) QUIZ_REWARD, quiz.getId());
      if (firstTime) {
        awarded = QUIZ_REWARD;
        walletBalance = pointService.getBalance(userId);
      }
    }

    // 시도 기록
    QuizAttempt attempt = new QuizAttempt();
    attempt.setUserId(userId);
    attempt.setQuizDate(today);
    attempt.setAttemptNo(1);
    attempt.setAnswer(userAnswer);
    attempt.setIsCorrect(correct);
    attempt.setPointsAwarded(awarded);
    attemptRepo.save(attempt);

    return new AnswerResult(
        correct ? "CORRECT" : "WRONG", awarded, walletBalance, quiz.getIngredient().getId());
  }

  // DTO
  @Getter
  @AllArgsConstructor
  public static class AnswerResult {

    private String result; // CORRECT | WRONG
    private Integer awarded; // 적립 포인트
    private Long walletBalance; // 적립 후 잔액 (null 가능)
    private Long ingredientId;
  }
}
