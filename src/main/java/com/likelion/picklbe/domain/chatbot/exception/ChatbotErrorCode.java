package com.likelion.picklbe.domain.chatbot.exception;

import org.springframework.http.HttpStatus;

import com.likelion.picklbe.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatbotErrorCode implements BaseErrorCode {
  CONVERSATION_NOT_FOUND("CHATBOT_404_1", "존재하지 않는 대화입니다.", HttpStatus.NOT_FOUND),
  MESSAGE_NOT_FOUND("CHATBOT_404_2", "존재하지 않는 메시지입니다.", HttpStatus.NOT_FOUND),
  MEMORY_NOT_FOUND("CHATBOT_404_3", "존재하지 않는 사용자 메모리입니다.", HttpStatus.NOT_FOUND),
  CHATBOT_REQUEST_FAILED(
      "CHATBOT_500_1", "챗봇 API 요청 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
