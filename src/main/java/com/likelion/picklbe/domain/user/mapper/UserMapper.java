package com.likelion.picklbe.domain.user.mapper;

import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.user.dto.response.SignUpResponse;
import com.likelion.picklbe.domain.user.entity.User;

@Component
public class UserMapper {

  public SignUpResponse toSignUpResponse(User user) {
    return SignUpResponse.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .intro(user.getIntro())
        .build();
  }
}
