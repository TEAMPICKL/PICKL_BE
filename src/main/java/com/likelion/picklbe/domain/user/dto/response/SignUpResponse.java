package com.likelion.picklbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "SignUpResponse DTO", description = "사용자 회원가입에 대한 응답 반환")
public class SignUpResponse {

  @Schema(description = "회원가입된 사용자 ID", example = "1")
  private Long userId;

  @Schema(description = "회원가입된 사용자 아이디", example = "zmarzmar")
  private String username;

  @Schema(description = "회원가입된 사용자 이메일", example = "zmarzmar@example.com")
  private String email;

  @Schema(description = "회원가입된 사용자 닉네임", example = "zmar")
  private String nickname;

  @Schema(description = "회원가입된 사용자의 자기소개", example = "안녕하세요. 저는...")
  private String intro;
}
