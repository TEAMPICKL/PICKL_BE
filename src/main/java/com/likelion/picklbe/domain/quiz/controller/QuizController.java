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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "퀴즈 API", description = "일일 OX 퀴즈 관련 API")
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

  private final QuizService quizService;
  private final DailyQuizRepository dailyQuizRepo;
  private final QuizAttemptRepository attemptRepo;

  @Operation(summary = "오늘의 퀴즈 조회", description = "로그인한 사용자의 오늘의 OX 퀴즈를 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "퀴즈 조회 성공"),
    @ApiResponse(responseCode = "401", description = "인증 실패"),
    @ApiResponse(responseCode = "404", description = "오늘의 퀴즈가 존재하지 않음")
  })
  @GetMapping("/daily")
  public QuizDailyResponse getDaily(@Parameter(hidden = true) @AuthUser Long userId) {
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

  @Operation(summary = "오늘의 퀴즈 정답 제출", description = "정답을 제출하고, 결과/포인트/다음 이동 정보를 반환합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "정답 처리 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이미 응시함"),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @PostMapping("/daily/answer")
  public QuizAnswerResult answer(
      @Parameter(hidden = true) @AuthUser Long userId, @Valid @RequestBody QuizAnswerRequest req) {

    var result = quizService.answer(userId, req.asBoolean(), req.getIdempotencyKey());

    boolean isCorrect = "CORRECT".equalsIgnoreCase(result.getResult());
    String cta = isCorrect ? "PRICE_CURRENT" : "PRICE_TODAY";
    String actionLabel = isCorrect ? "현재가 보기" : "오늘의 가격 보러가기";

    // 프론트 라우트 규칙에 맞춘 경로/딥링크 (예: /price/{ingredientId}?view=current|today)
    String actionPath =
        "/price/" + result.getIngredientId() + (isCorrect ? "?view=current" : "?view=today");

    return QuizAnswerResult.builder()
        .result(result.getResult())
        .awarded(result.getAwarded())
        .walletBalance(result.getWalletBalance())
        .ingredientId(result.getIngredientId())
        .cta(cta)
        .actionPath(actionPath)
        .actionLabel(actionLabel)
        .build();
  }

  @Operation(summary = "오늘의 퀴즈 강제 생성 (운영용)", description = "관리자 권한으로 오늘의 퀴즈를 강제로 생성합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "퀴즈 생성 성공"),
    @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @PostMapping("/daily/admin/force-generate")
  public Map<String, String> forceGenerate() {
    DailyQuiz dq = quizService.createTodayQuizIfAbsent();
    return Map.of("quizDate", dq.getQuizDate().toString(), "id", String.valueOf(dq.getId()));
  }
}
