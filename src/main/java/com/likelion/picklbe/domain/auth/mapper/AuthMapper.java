package com.likelion.picklbe.domain.auth.mapper;

import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.auth.dto.response.LoginResponse;
import com.likelion.picklbe.domain.user.entity.User;

@Component
public class AuthMapper {

  public LoginResponse toLoginResponse(
      User user, String accessToken, Long expirationTime, String refreshToken) {
    return LoginResponse.builder()
        .accessToken(accessToken)
        .userId(user.getId())
        .username(user.getUsername())
        .role(user.getRole())
        .expirationTime(expirationTime)
        .refreshToken(refreshToken)
        .build();
  }
}
