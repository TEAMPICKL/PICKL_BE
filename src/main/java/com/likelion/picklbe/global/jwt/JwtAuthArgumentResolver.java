package com.likelion.picklbe.global.jwt;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.likelion.picklbe.global.exception.CustomException;
import com.likelion.picklbe.global.exception.ErrorCode;
import com.likelion.picklbe.global.security.annotation.AuthUser;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(AuthUser.class)
        && parameter.getParameterType() == Long.class;
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED); // <-- 여기!
    }

    Object principal = auth.getPrincipal();

    // 1) CustomUserDetails에 getId()가 있는 경우
    try {
      var method = principal.getClass().getMethod("getId");
      Object id = method.invoke(principal);
      if (id instanceof Long) {
        return id;
      }
      if (id instanceof Number) {
        return ((Number) id).longValue();
      }
    } catch (ReflectiveOperationException ignore) {
    }

    // 2) CustomUserDetails.getUser().getId() 패턴인 경우
    try {
      var getUser = principal.getClass().getMethod("getUser");
      Object user = getUser.invoke(principal);
      var getId = user.getClass().getMethod("getId");
      Object id = getId.invoke(user);
      if (id instanceof Long) {
        return id;
      }
      if (id instanceof Number) {
        return ((Number) id).longValue();
      }
    } catch (ReflectiveOperationException ignore) {
    }

    // 3) 그래도 못 찾으면 401
    throw new CustomException(ErrorCode.UNAUTHORIZED);
  }
}
