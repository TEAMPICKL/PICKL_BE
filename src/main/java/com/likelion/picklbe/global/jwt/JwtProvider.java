package com.likelion.picklbe.global.jwt;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.auth.exception.AuthErrorCode;
import com.likelion.picklbe.global.exception.CustomException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {

  private final Key key;
  private final long accessTokenExpireTime;
  private final long refreshTokenExpireTime;

  public JwtProvider(
      @Value("${spring.jwt.secret}") String secretKey,
      @Value("${spring.jwt.access-token-expire-time}") long accessTokenExpireTime,
      @Value("${spring.jwt.refresh-token-expire-time}") long refreshTokenExpireTime) {
    byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenExpireTime = accessTokenExpireTime;
    this.refreshTokenExpireTime = refreshTokenExpireTime;
  }

  /** 기존 방식 유지: sub=username, jti=username */
  public String createAccessToken(String username) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(username)
        .setId(String.valueOf(username))
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + accessTokenExpireTime))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /** 개선: userId 클레임을 추가(선택). 구토큰과 하위호환 유지 */
  public String createAccessToken(String username, Long userId) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(username) // 그대로
        .setId(String.valueOf(username)) // 그대로(하위호환: jti를 username으로 유지)
        .claim("userId", userId) // 신규 클레임
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + accessTokenExpireTime))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public long getExpiration(String accessToken) {
    Claims claims = parseClaims(accessToken);
    Date expiration = claims.getExpiration();
    long now = System.currentTimeMillis();
    return expiration.getTime() - now;
  }

  private Key getSigningKey() {
    return key;
  }

  public String createRefreshToken(String username, String tokenId) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(username)
        .setId(tokenId)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + refreshTokenExpireTime))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      throw new CustomException(AuthErrorCode.JWT_TOKEN_EXPIRED);
    } catch (UnsupportedJwtException e) {
      throw new CustomException(AuthErrorCode.UNSUPPORTED_TOKEN);
    } catch (MalformedJwtException e) {
      throw new CustomException(AuthErrorCode.MALFORMED_JWT_TOKEN);
    } catch (io.jsonwebtoken.SignatureException e) {
      throw new CustomException(AuthErrorCode.INVALID_SIGNATURE);
    } catch (IllegalArgumentException e) {
      throw new CustomException(AuthErrorCode.ILLEGAL_ARGUMENT);
    }
  }

  /** 기존: username(또는 socialId)을 sub에서 추출 */
  public String extractSocialId(String token) {
    return parseClaims(token).getSubject();
  }

  public String extractTokenId(String token) {
    return parseClaims(token).getId();
  }

  /** 신규: userId 클레임이 있으면 반환, 없으면 Optional.empty() */
  public Optional<Long> extractUserIdIfPresent(String token) {
    Claims c = parseClaims(token);
    Object v = c.get("userId");
    if (v == null) {
      return Optional.empty();
    }
    try {
      if (v instanceof Number) {
        return Optional.of(((Number) v).longValue());
      }
      return Optional.of(Long.parseLong(String.valueOf(v)));
    } catch (Exception ignore) {
      return Optional.empty();
    }
  }

  /** 편의: username(sub) 바로 추출 */
  public String extractUsername(String token) {
    return extractSocialId(token);
  }

  private Claims parseClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
