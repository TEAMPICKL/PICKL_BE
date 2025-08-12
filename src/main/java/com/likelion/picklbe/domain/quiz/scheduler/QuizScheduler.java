package com.likelion.picklbe.domain.quiz.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.quiz.service.QuizService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuizScheduler {

  private final QuizService quizService;

  @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
  public void generateDaily() {
    quizService.createTodayQuizIfAbsent();
  }
}
