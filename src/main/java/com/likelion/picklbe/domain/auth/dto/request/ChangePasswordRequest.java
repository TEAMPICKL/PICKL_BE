package com.likelion.picklbe.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

@Getter
public class ChangePasswordRequest {

  @NotBlank(message = "사용자 아이디 항목은 필수입니다.")
  private String username;

  @NotBlank(message = "사용자 아이디 항목은 필수입니다.")
  private String currentPassword;

  @NotBlank(message = "사용자 아이디 항목은 필수입니다.")
  private String newPassword;
}
