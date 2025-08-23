package com.likelion.picklbe.domain.brand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BrandImageResolver {

  // ex) https://picklocal.s3.ap-northeast-2.amazonaws.com
  @Value("${app.cdn.base-url:https://picklocal.s3.ap-northeast-2.amazonaws.com}")
  private String baseUrl;

  @Value("${app.cdn.brand-path:images/brand}")
  private String brandPath;

  @Value("${app.cdn.default-file:mart_default.png}")
  private String defaultFile;

  /**
   * 원문(지점명/상호명/브랜드명 포함 가능)에서 Brand enum을 추정
   */
  public Brand resolveBrand(String raw) {
    if (!StringUtils.hasText(raw)) {
      return Brand.DEFAULT;
    }
    return Brand.fromStoreName(raw);
  }

  /**
   * explicitBrand(컬럼) 우선, 없으면 name(지점명)으로 추정
   */
  public Brand resolveBrand(String explicitBrand, String name) {
    if (StringUtils.hasText(explicitBrand)) {
      // fromStoreName 가 브랜드명도 파싱한다는 전제
      return resolveBrand(explicitBrand);
    }
    return resolveBrand(name);
  }

  /**
   * Brand 코드만 필요할 때
   */
  public String resolveBrandCode(String raw) {
    return resolveBrand(raw).code();
  }

  /**
   * Brand 코드만 이미 있는 경우(예: DB 조회)
   */
  public String resolveBrandCodeFromCode(String brandCode) {
    Brand b = Brand.fromCodeSafe(brandCode); // 없으면 DEFAULT 반환하도록 enum에 구현되어 있어야 함
    return b.code();
  }

  /**
   * 원문에서 바로 대표 이미지 URL
   */
  public String resolveImageUrl(String raw) {
    return imageUrlFor(resolveBrand(raw));
  }

  /**
   * explicitBrand + name 동시 고려
   */
  public String resolveImageUrl(String explicitBrand, String name) {
    return imageUrlFor(resolveBrand(explicitBrand, name));
  }

  /**
   * Brand가 이미 있는 경우 이미지 URL
   */
  public String imageUrlFor(Brand brand) {
    // 1) brand가 null/DEFAULT 이거나 파일명이 비어있으면 기본 이미지
    String filename =
        (brand == null || brand == Brand.DEFAULT || !StringUtils.hasText(brand.filename()))
            ? defaultFile
            : brand.filename();

    // 2) filename 자체가 절대 URL이면 그대로 반환 (운영 중 전체 URL을 enum에 넣은 경우 대응)
    if (isAbsoluteUrl(filename)) {
      return filename;
    }

    // 3) baseUrl/brandPath 안전 조합
    return joinUrl(rtrimSlashes(baseUrl), rtrimSlashes(brandPath), filename);
  }

  /**
   * Brand 코드로 바로 이미지 URL (코드만 내려받는 API/쿼리 대응)
   */
  public String imageUrlForCode(String brandCode) {
    Brand b = Brand.fromCodeSafe(brandCode);
    return imageUrlFor(b);
  }

  /**
   * 원문에서 Brand와 이미지 URL/코드를 한 번에
   */
  public ResolvedBrand resolve(String raw) {
    Brand b = resolveBrand(raw);
    return new ResolvedBrand(b, imageUrlFor(b), b.code());
  }

  /**
   * explicitBrand + name 동시 입력 버전
   */
  public ResolvedBrand resolve(String explicitBrand, String name) {
    Brand b = resolveBrand(explicitBrand, name);
    return new ResolvedBrand(b, imageUrlFor(b), b.code());
  }

  // ---- helpers -------------------------------------------------------------

  private static boolean isAbsoluteUrl(String s) {
    if (!StringUtils.hasText(s)) {
      return false;
    }
    String t = s.trim().toLowerCase();
    return t.startsWith("http://") || t.startsWith("https://");
  }

  private static String rtrimSlashes(String s) {
    if (!StringUtils.hasText(s)) {
      return "";
    }
    return s.replaceAll("/+$", "");
  }

  private static String joinUrl(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      String p = parts[i] == null ? "" : parts[i].trim();
      if (p.isEmpty()) {
        continue;
      }
      if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
        sb.append('/');
      }
      sb.append(p.replaceAll("^/+", "")); // 앞 슬래시 제거
    }
    return sb.toString();
  }

  /**
   * 편의 반환 DTO
   */
  public record ResolvedBrand(Brand brand, String imageUrl, String code) {

  }
}

