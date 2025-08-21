package com.likelion.picklbe.domain.brand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BrandImageResolver {

  @Value("${app.cdn.base-url:https://picklocal.s3.ap-northeast-2.amazonaws.com}")
  private String baseUrl;

  @Value("${app.cdn.brand-path:brands}")
  private String brandPath;

  @Value("${app.cdn.default-file:mart_default.png}")
  private String defaultFile;

  /** 원문(지점명/상호명/브랜드명 포함 가능)에서 Brand enum을 추정 */
  public Brand resolveBrand(String raw) {
    if (raw == null || raw.isBlank()) {
      return Brand.DEFAULT;
    }
    return Brand.fromStoreName(raw);
  }

  /** Brand 코드만 필요할 때 */
  public String resolveBrandCode(String raw) {
    return resolveBrand(raw).code();
  }

  /** 원문에서 바로 대표 이미지 URL */
  public String resolveImageUrl(String raw) {
    return imageUrlFor(resolveBrand(raw));
  }

  /** Brand가 이미 있는 경우 이미지 URL */
  public String imageUrlFor(Brand brand) {
    String filename =
        (brand == null || brand == Brand.DEFAULT || brand.filename() == null)
            ? defaultFile
            : brand.filename();
    return String.format("%s/%s/%s", rtrim(baseUrl), rtrim(brandPath), filename);
  }

  /** 원문에서 Brand와 이미지 URL을 한 번에 */
  public ResolvedBrand resolve(String raw) {
    Brand b = resolveBrand(raw);
    return new ResolvedBrand(b, imageUrlFor(b), b.code());
  }

  private String rtrim(String s) {
    if (s == null || s.isBlank()) {
      return "";
    }
    return s.replaceAll("/+$", "");
  }

  /** 편의 반환 DTO */
  public record ResolvedBrand(Brand brand, String imageUrl, String code) {}
}
