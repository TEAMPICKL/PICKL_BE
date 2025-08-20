package com.likelion.picklbe.domain.quiz.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.point.service.PointService;
import com.likelion.picklbe.domain.quiz.dto.AnswerResult;
import com.likelion.picklbe.domain.quiz.dto.response.QuizDailyResponse;
import com.likelion.picklbe.domain.quiz.entity.DailyQuiz;
import com.likelion.picklbe.domain.quiz.entity.QuizAttempt;
import com.likelion.picklbe.domain.quiz.entity.QuizDailyAttemptToken;
import com.likelion.picklbe.domain.quiz.entity.QuizPool;
import com.likelion.picklbe.domain.quiz.entity.UserDailyQuestion;
import com.likelion.picklbe.domain.quiz.repository.DailyQuizRepository;
import com.likelion.picklbe.domain.quiz.repository.QuizAttemptRepository;
import com.likelion.picklbe.domain.quiz.repository.QuizDailyAttemptTokenRepository;
import com.likelion.picklbe.domain.quiz.repository.QuizPoolRepository;
import com.likelion.picklbe.domain.quiz.repository.UserDailyQuestionRepository;
import com.likelion.picklbe.global.exception.ApiException;
import com.likelion.picklbe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizDailyService {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final int QUIZ_REWARD = 100;
  private static final int N_DAYS_NO_REPEAT = 7;

  private final DailyQuizRepository dailyQuizRepo;
  private final QuizAttemptRepository attemptRepo;
  private final QuizPoolRepository quizPoolRepo;
  private final QuizDailyAttemptTokenRepository tokenRepo;
  private final UserDailyQuestionRepository udqRepo;
  private final PointService pointService;

  @Transactional
  public QuizDailyResponse getToday(Long userId) {
    LocalDate today = LocalDate.now(KST);

    int attempts = attemptRepo.countByUserIdAndQuizDate(userId, today);
    int extra = getExtraGrantedToday(userId, today);
    int remaining = Math.max(0, 1 + extra - attempts);
    int nextNo = attempts + 1;

    Long quizPoolIdToShow;

    if (nextNo == 1) {
      DailyQuiz dq =
          dailyQuizRepo
              .findByQuizDate(today)
              .orElseThrow(() -> ApiException.of(ErrorCode.QUIZ_NOT_READY));
      return QuizDailyResponse.of(today, dq.getIngredient(), dq.getStatement(), remaining);
    }

    // 2회차 이상: 필요 시 배정 저장(쓰기)
    var assigned = udqRepo.findOne(userId, today, nextNo);
    if (assigned.isPresent()) {
      quizPoolIdToShow = assigned.get().getQuizPoolId();
    } else {
      List<Long> used = attemptRepo.findQuizPoolIdsByUserAndDate(userId, today);
      var page = PageRequest.of(0, 1);

      var pickedList = quizPoolRepo.pickOneExcluding(used, used.size(), page);

      if (pickedList.isEmpty()) {
        var cycled = quizPoolRepo.pickOneExcluding(Collections.emptyList(), 0, page);
        if (cycled.isEmpty()) {
          throw ApiException.of(ErrorCode.QUIZ_POOL_EMPTY);
        }
        quizPoolIdToShow = cycled.get(0).getId();
      } else {
        quizPoolIdToShow = pickedList.get(0).getId();
      }

      UserDailyQuestion row = new UserDailyQuestion();
      row.setUserId(userId);
      row.setQuizDate(today);
      row.setAttemptNo(nextNo);
      row.setQuizPoolId(quizPoolIdToShow);
      udqRepo.save(row); // write
    }

    QuizPool qp =
        quizPoolRepo
            .findById(quizPoolIdToShow)
            .orElseThrow(() -> new IllegalStateException("배정된 퀴즈가 존재하지 않습니다."));
    return QuizDailyResponse.of(today, qp.getIngredient(), qp.getStatement(), remaining);
  }

  @Transactional
  public AnswerResult submit(Long userId, boolean answer) {
    LocalDate today = LocalDate.now(KST);

    int attempts = attemptRepo.countByUserIdAndQuizDate(userId, today);
    int extra = getExtraGrantedToday(userId, today);
    int remaining = Math.max(0, 1 + extra - attempts);
    if (remaining <= 0) {
      throw ApiException.of(ErrorCode.ALREADY_ATTEMPTED);
    }

    int nextNo = attempts + 1;

    Long quizPoolId;
    if (nextNo == 1) {
      DailyQuiz dq =
          dailyQuizRepo
              .findByQuizDate(today)
              .orElseThrow(() -> ApiException.of(ErrorCode.QUIZ_NOT_READY));
      quizPoolId = dq.getQuizPool().getId();
    } else {
      quizPoolId =
          udqRepo
              .findOne(userId, today, nextNo)
              .map(UserDailyQuestion::getQuizPoolId)
              .orElseGet(
                  () -> {
                    List<Long> used = attemptRepo.findQuizPoolIdsByUserAndDate(userId, today);
                    var page = PageRequest.of(0, 1);
                    var picked = quizPoolRepo.pickOneExcluding(used, used.size(), page);

                    if (picked.isEmpty()) {
                      var cycled = quizPoolRepo.pickOneExcluding(Collections.emptyList(), 0, page);
                      if (cycled.isEmpty()) {
                        throw ApiException.of(ErrorCode.QUIZ_POOL_EMPTY);
                      }
                      Long pid = cycled.get(0).getId();
                      UserDailyQuestion row = new UserDailyQuestion();
                      row.setUserId(userId);
                      row.setQuizDate(today);
                      row.setAttemptNo(nextNo);
                      row.setQuizPoolId(pid);
                      udqRepo.save(row);
                      return pid;
                    }

                    Long pid = picked.get(0).getId();
                    UserDailyQuestion row = new UserDailyQuestion();
                    row.setUserId(userId);
                    row.setQuizDate(today);
                    row.setAttemptNo(nextNo);
                    row.setQuizPoolId(pid);
                    udqRepo.save(row);
                    return pid;
                  });
    }

    QuizPool qp = quizPoolRepo.findById(quizPoolId).orElseThrow();
    boolean correct = Boolean.TRUE.equals(qp.getAnswer()) == answer;
    int awarded = 0;
    Long walletBalance = null;

    if (correct) {
      boolean firstTime = pointService.earnDailyQuizOnce(userId, (long) QUIZ_REWARD, qp.getId());
      if (firstTime) {
        awarded = QUIZ_REWARD;
        walletBalance = pointService.getBalance(userId);
      }
    }

    QuizAttempt attempt = new QuizAttempt();
    attempt.setUserId(userId);
    attempt.setQuizDate(today);
    attempt.setAttemptNo(nextNo);
    attempt.setQuizPoolId(quizPoolId);
    attempt.setAnswer(answer);
    attempt.setIsCorrect(correct);
    attempt.setPointsAwarded(awarded);
    attemptRepo.save(attempt);

    return new AnswerResult(
        correct ? "CORRECT" : "WRONG", awarded, walletBalance, qp.getIngredient().getId());
  }

  // 내부에서 FOR UPDATE를 사용하므로 이 메서드는 반드시 write 트랜잭션 안에서 호출되어야 함
  private int getExtraGrantedToday(Long userId, LocalDate today) {
    return tokenRepo
        .findByUserIdAndDateForUpdate(userId, today)
        .map(QuizDailyAttemptToken::getTokens)
        .orElse(0);
  }

  @Transactional
  public DailyQuiz createTodayQuizIfAbsent() {
    LocalDate today = LocalDate.now(KST);

    return dailyQuizRepo
        .findByQuizDate(today)
        .orElseGet(
            () -> {
              LocalDate threshold = today.minusDays(N_DAYS_NO_REPEAT);
              List<QuizPool> candidates =
                  quizPoolRepo.findPickableRandom(threshold, PageRequest.of(0, 10));

              if (candidates.isEmpty()) {
                candidates = quizPoolRepo.findRandom(PageRequest.of(0, 10));
              }
              if (candidates.isEmpty()) {
                throw ApiException.of(ErrorCode.QUIZ_POOL_EMPTY);
              }

              QuizPool picked = candidates.get(0);
              picked.setLastUsedDate(today);

              DailyQuiz dq = new DailyQuiz();
              dq.setQuizDate(today);
              dq.setQuizPool(picked);
              dq.setIngredient(picked.getIngredient());
              dq.setStatement(picked.getStatement());
              dq.setAnswer(picked.getAnswer());
              return dailyQuizRepo.save(dq);
            });
  }
}
