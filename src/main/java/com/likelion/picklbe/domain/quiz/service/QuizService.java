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
              // pick one from pool
              LocalDate threshold = today.minusDays(N_DAYS_NO_REPEAT);
              List<QuizPool> candidates =
                  quizPoolRepo.findPickableRandom(threshold, PageRequest.of(0, 10));
              if (candidates.isEmpty()) {
                throw ApiException.of(ErrorCode.QUIZ_POOL_EMPTY);
              }

              QuizPool picked = candidates.get(0);
              picked.setLastUsedDate(today); // mark used
              // persist via repo (dirty checking handles it)

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

    // 포인트/지갑 처리 (idempotent)
    int awarded = 0;
    Long walletBalance = null;

    if (correct) {
      // 정답인 경우에만 일일 퀴즈 적립을 시도 (중복 적립 방지)
      boolean firstTime = pointService.earnDailyQuizOnce(userId, (long) QUIZ_REWARD, quiz.getId());
      if (firstTime) {
        awarded = QUIZ_REWARD;
        // 적립 직후 잔액 조회 (earnDailyQuizOnce가 잔액을 리턴하도록 확장 가능)
        walletBalance = pointService.getBalance(userId);
      }
      // firstTime=false 면 이미 적립한 케이스 → awarded=0, walletBalance=null 유지
      // 필요 시 DTO에 'alreadyRewarded' 같은 플래그 추가 가능
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
    private Integer awarded;
    private Long walletBalance; // null if 0 or 미적립
    private Long ingredientId;
  }
}
