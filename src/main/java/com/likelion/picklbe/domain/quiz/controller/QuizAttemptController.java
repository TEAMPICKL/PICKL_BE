package com.likelion.picklbe.domain.quiz.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.quiz.dto.response.ExtraAttemptResponse;
import com.likelion.picklbe.domain.quiz.service.AdRewardService;
import com.likelion.picklbe.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quiz/attempts")
@RequiredArgsConstructor
@Tag(name = "퀴즈 시도 관리", description = """
            퀴즈 시도 횟수 및 추가 시도권 관리 API
            """)
public class QuizAttemptController {

  private final AdRewardService adRewardService;

  @PostMapping("/extra")
  @Operation(summary = "광고 시청 보상: 추가 시도 1회 지급")
  public ResponseEntity<ExtraAttemptResponse> grantExtraAttempt(
      @AuthenticationPrincipal CustomUserDetails user) {
    int remaining = adRewardService.grantExtraAttempt(user.getId());
    return ResponseEntity.ok(new ExtraAttemptResponse(remaining));
  }
}
