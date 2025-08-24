package com.likelion.picklbe.domain.auth.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.auth.dto.request.ChangePasswordRequest;
import com.likelion.picklbe.domain.auth.dto.request.LoginRequest;
import com.likelion.picklbe.domain.auth.dto.response.LoginResponse;
import com.likelion.picklbe.domain.auth.mapper.AuthMapper;
import com.likelion.picklbe.domain.user.entity.User;
import com.likelion.picklbe.domain.user.exception.UserErrorCode;
import com.likelion.picklbe.domain.user.repository.UserRepository;
import com.likelion.picklbe.global.exception.CustomException;
import com.likelion.picklbe.global.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;
  private final AuthMapper authMapper;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public LoginResponse login(LoginRequest loginRequest) {
    // 사용자 조회
    User user =
        userRepository
            .findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 인증 처리
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(), loginRequest.getPassword());
    authenticationManager.authenticate(authenticationToken);

    // 액세스 토큰 및 리프레시 토큰 발급
    String accessToken = jwtProvider.createAccessToken(user.getUsername(), user.getId());
    String refreshToken =
        jwtProvider.createRefreshToken(user.getUsername(), UUID.randomUUID().toString());

    // 리프레시 토큰 저장
    user.createRefreshToken(refreshToken);

    // Access Token의 만료 시간
    Long expirationTime = jwtProvider.getExpiration(accessToken);

    // 로그인 성공 로그
    log.info("로그인 성공: {}", user.getUsername());

    // 로그인 응답 반환
    return authMapper.toLoginResponse(user, accessToken, expirationTime, refreshToken);
  }

  @Transactional
  public void changePassword(ChangePasswordRequest request) {
    User user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 현재 비밀번호 일치 여부 확인
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new CustomException(UserErrorCode.INVALID_PASSWORD);
    }

    // 새 비밀번호 암호화 후 저장
    user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
  }
}
