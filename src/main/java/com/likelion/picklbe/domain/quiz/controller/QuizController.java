package com.likelion.picklbe.domain.quiz.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.quiz.dto.QuizAnswerRequest;
import com.likelion.picklbe.domain.quiz.dto.QuizAnswerResult;
import com.likelion.picklbe.domain.quiz.dto.QuizDailyResponse;
import com.likelion.picklbe.domain.quiz.entity.DailyQuiz;
import com.likelion.picklbe.domain.quiz.repository.DailyQuizRepository;
import com.likelion.picklbe.domain.quiz.repository.QuizAttemptRepository;
import com.likelion.picklbe.domain.quiz.service.QuizService;
import com.likelion.picklbe.global.security.annotation.AuthUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

  private final QuizService quizService;
  private final DailyQuizRepository dailyQuizRepo;
  private final QuizAttemptRepository attemptRepo;

  @GetMapping("/api/quiz/daily")
  public QuizDailyResponse getDaily(@AuthUser Long userId) {
    DailyQuiz dq = quizService.getOrThrowTodayQuiz();
    boolean attempted = attemptRepo.existsByUserIdAndQuizDate(userId, dq.getQuizDate());

    return QuizDailyResponse.builder()
        .date(dq.getQuizDate().toString())
        .statement(dq.getStatement())
        .options(List.of("O", "X"))
        .attempted(attempted)
        .ingredient(
            QuizDailyResponse.IngredientDto.builder()
                .id(dq.getIngredient().getId())
                .name(dq.getIngredient().getName())
                .iconUrl(dq.getIngredient().getIconUrl())
                .build())
        .build();
  }

  @PostMapping("/api/quiz/daily/answer")
  public QuizAnswerResult answer(@AuthUser Long userId, @Valid @RequestBody QuizAnswerRequest req) {
    var result = quizService.answer(userId, req.asBoolean(), req.getIdempotencyKey());
    return QuizAnswerResult.builder()
        .result(result.getResult())
        .awarded(result.getAwarded())
        .walletBalance(result.getWalletBalance())
        .cta("PRICE_DETAIL")
        .ingredientId(result.getIngredientId())
        .build();
  }

  // 운영용 (관리자만 호출) : 오늘 퀴즈 강제 생성
  @PostMapping("/daily/admin/force-generate")
  public Map<String, String> forceGenerate() {
    DailyQuiz dq = quizService.createTodayQuizIfAbsent();
    return Map.of("quizDate", dq.getQuizDate().toString(), "id", String.valueOf(dq.getId()));
  }
}
