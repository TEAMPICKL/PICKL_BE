package com.likelion.picklbe.domain.history.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.history.dto.MessageDto;
import com.likelion.picklbe.domain.history.dto.SessionSummaryDto;
import com.likelion.picklbe.domain.history.service.ChatHistoryReadService;
import com.likelion.picklbe.global.response.BaseResponse;
import com.likelion.picklbe.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "채팅 기록 조회 API", description = "사용자의 채팅 세션 및 메시지 기록을 조회하는 API")
public class ChatHistoryReadController {

  private final ChatHistoryReadService service;

  @GetMapping("/sessions/simple")
  @Operation(
      summary = "세션 목록 조회(기본값)",
      description = "항상 page=0, size=20, sort=modifiedAt,DESC로 반환합니다.")
  public BaseResponse<Page<SessionSummaryDto>> sessionsSimple(
      @AuthenticationPrincipal CustomUserDetails me) {
    Pageable pageable = PageRequest.of(0, 20, Sort.by("modifiedAt").descending());
    return BaseResponse.success("세션 목록 조회 성공", service.list(me.getId(), pageable));
  }

  @Operation(summary = "세션 개수 조회", description = "사용자가 가진 전체 채팅 세션의 개수를 반환합니다.")
  @GetMapping("/sessions/count")
  public BaseResponse<Long> count(@AuthenticationPrincipal CustomUserDetails me) {
    return BaseResponse.success("세션 개수 조회 성공", service.count(me.getId()));
  }

  @Operation(summary = "메시지 조회", description = "특정 세션의 모든 메시지를 오름차순으로 전체 조회합니다.")
  @GetMapping("/sessions/{sessionId}/messages")
  public BaseResponse<List<MessageDto>> messages(
      @AuthenticationPrincipal CustomUserDetails me,
      @Parameter(description = "세션 ID", required = true) @PathVariable Long sessionId) {
    return BaseResponse.success("메시지 조회 성공", service.messages(me.getId(), sessionId));
  }
}
