package com.likelion.picklbe.domain.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.auth.dto.request.ChangePasswordRequest;
import com.likelion.picklbe.domain.auth.dto.request.LoginRequest;
import com.likelion.picklbe.domain.auth.dto.response.LoginResponse;
import com.likelion.picklbe.domain.auth.service.AuthService;
import com.likelion.picklbe.domain.user.entity.User;
import com.likelion.picklbe.domain.user.repository.UserRepository;
import com.likelion.picklbe.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auths")
@Tag(name = "Auth", description = "Auth 관리 API")
public class AuthController {

  private final AuthService authService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Operation(summary = "사용자 로그인", description = "사용자 로그인을 위한 API")
  @PostMapping("/login")
  public ResponseEntity<BaseResponse<LoginResponse>> login(
      @RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {

    LoginResponse loginResponse = authService.login(loginRequest);

    // refreshToken 가져오기
    String refreshToken = loginResponse.getRefreshToken();

    // Set-Cookie 설정 (HttpOnly + Secure)
    Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true); // HTTPS일 때만
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 7일

    response.addCookie(refreshTokenCookie);

    return ResponseEntity.ok(BaseResponse.success("로그인에 성공했습니다.", loginResponse));
  }

  @PostMapping("/test-login")
  @Operation(summary = "테스트용 간편 로그인", description = "버튼 클릭만으로 로그인되는 테스트 계정용 API")
  public ResponseEntity<BaseResponse<LoginResponse>> testLogin(HttpServletResponse response) {
    // 1. 테스트 계정 정보
    String username = "testuser";
    String password = "testpassword";

    // 2. 계정이 없으면 자동 생성
    if (!userRepository.existsByUsername(username)) {
      User testUser =
          User.builder()
              .username(username)
              .password(passwordEncoder.encode(password))
              .email("test@pickl.com")
              .nickname("테스트계정")
              .intro("테스트용 계정입니다.")
              .build();
      userRepository.save(testUser);
    }

    // 3. 로그인 처리
    LoginRequest loginRequest = new LoginRequest(username, password);
    LoginResponse loginResponse = authService.login(loginRequest);

    // 4. refreshToken 쿠키 설정
    Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 7일

    response.addCookie(refreshTokenCookie);

    return ResponseEntity.ok(BaseResponse.success("테스트 로그인 성공!", loginResponse));
  }

  @PatchMapping("/password")
  public ResponseEntity<BaseResponse<String>> changePassword(
      @RequestBody @Valid ChangePasswordRequest request) {
    authService.changePassword(request);
    return ResponseEntity.ok(BaseResponse.success("비밀번호가 성공적으로 변경되었습니다.", null));
  }
}
