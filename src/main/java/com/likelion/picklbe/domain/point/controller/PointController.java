package com.likelion.picklbe.domain.point.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.point.entity.PointTx;
import com.likelion.picklbe.domain.point.service.PointService;
import com.likelion.picklbe.global.security.annotation.AuthUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "포인트 API", description = "포인트 지갑 잔액 및 내역 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

  private final PointService pointService;

  /** 내 지갑 잔액 */
  @Operation(summary = "내 지갑 잔액 조회", description = "인증된 사용자의 현재 포인트 지갑 잔액을 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "잔액 조회 성공"),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/wallet")
  public ResponseEntity<?> getWallet(@Parameter(hidden = true) @AuthUser Long userId) {
    long balance = pointService.getBalance(userId);
    return ResponseEntity.ok(new WalletResponse(balance));
  }

  /** 내 포인트 내역 */
  @Operation(summary = "내 포인트 내역 조회", description = "인증된 사용자의 포인트 적립/차감 내역을 페이지 형태로 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "내역 조회 성공"),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/history")
  public ResponseEntity<?> getHistory(
      @Parameter(hidden = true) @AuthUser Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<PointTx> history = pointService.getHistory(userId, PageRequest.of(page, size));
    return ResponseEntity.ok(history);
  }

  /** 지갑 잔액 응답 DTO */
  record WalletResponse(long balance) {}
}
