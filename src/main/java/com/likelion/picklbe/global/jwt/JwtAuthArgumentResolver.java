package com.likelion.picklbe.global.jwt;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.likelion.picklbe.global.exception.CustomException;
import com.likelion.picklbe.global.security.annotation.AuthUser;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthArgumentResolver implements HandlerMethodArgumentResolver {

  private final JwtProvider jwtProvider; // 이미 존재하는 클래스 재사용

  @Override
  public boolean supportsParameter(MethodParameter p) {
    return p.hasParameterAnnotation(AuthUser.class) && p.getParameterType() == Long.class;
  }

  @Override
  public Object resolveArgument(
      MethodParameter p, ModelAndViewContainer mav, NativeWebRequest req, WebDataBinderFactory b) {
    HttpServletRequest http = (HttpServletRequest) req.getNativeRequest();
    String auth = http.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }
    String token = auth.substring(7);
    return jwtProvider.getUserId(token); // JwtProvider에 userId 추출 메서드가 있어야 함
  }
}
