package com.likelion.picklbe.domain.brand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BrandImageResolver {

  // 프로퍼티가 없으면 기본값으로 동작
  @Value("${app.cdn.base-url:https://picklocal.s3.ap-northeast-2.amazonaws.com}")
  private String baseUrl;

  @Value("${app.cdn.brand-path:brands}")
  private String brandPath;

  @Value("${app.cdn.default-file:mart_default.png}")
  private String defaultFile;

  public String resolveBrandCode(String storeName) {
    return Brand.fromStoreName(storeName).code();
  }

  public String resolveImageUrl(String storeName) {
    Brand b = Brand.fromStoreName(storeName);
    String filename = (b == Brand.DEFAULT || b.filename() == null) ? defaultFile : b.filename();
    // base-url/brand-path/filename (중복 슬래시 방지)
    return String.format("%s/%s/%s", rtrim(baseUrl), rtrim(brandPath), filename);
  }

  private String rtrim(String s) {
    if (s == null || s.isBlank()) {
      return "";
    }
    return s.replaceAll("/+$", "");
  }
}
