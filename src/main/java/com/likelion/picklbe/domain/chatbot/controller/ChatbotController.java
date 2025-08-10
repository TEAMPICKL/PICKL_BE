package com.likelion.picklbe.domain.chatbot.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatRequest;
import com.likelion.picklbe.domain.chatbot.dto.ChatDtos.ChatResponse;
import com.likelion.picklbe.domain.chatbot.service.ChatbotService;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Chatbot", description = "챗봇 대화 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
public class ChatbotController {

  private final ChatbotService service;

  @Operation(summary = "챗봇 대화", description = "첫 턴은 conversationId=null 로 호출합니다.")
  @PostMapping("/chat")
  public ResponseEntity<BaseResponse<ChatResponse>> chat(@RequestBody @Valid ChatRequest req) {
    ChatResponse res = service.chat(req);
    return ResponseEntity.ok(BaseResponse.success("ok", res));
  }
}
