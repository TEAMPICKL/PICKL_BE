package com.likelion.picklbe.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.user.dto.request.SignUpRequest;
import com.likelion.picklbe.domain.user.dto.response.SignUpResponse;
import com.likelion.picklbe.domain.user.entity.User;
import com.likelion.picklbe.domain.user.exception.UserErrorCode;
import com.likelion.picklbe.domain.user.mapper.UserMapper;
import com.likelion.picklbe.domain.user.repository.UserRepository;
import com.likelion.picklbe.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  @Transactional
  public SignUpResponse signUp(SignUpRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new CustomException(UserErrorCode.USERNAME_ALREADY_EXISTS);
    }

    if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
      throw new CustomException(UserErrorCode.PASSWORD_REQUIRED);
    }

    // 비밀번호 인코딩
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // 유저 엔티티 생성
    User user =
        User.builder()
            .username(request.getUsername())
            .password(encodedPassword)
            .email(request.getEmail())
            .nickname(request.getNickname())
            .intro(request.getIntro())
            .build();

    // 저장 및 로깅
    User savedUser = userRepository.save(user);
    log.info("New user registered: {}", savedUser.getUsername());

    return userMapper.toSignUpResponse(savedUser);
  }
}
