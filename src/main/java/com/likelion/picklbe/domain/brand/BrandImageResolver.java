package com.likelion.picklbe.domain.brand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BrandImageResolver {

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${cloud.aws.s3.path.market:images/market}")
  private String marketPath;

  @Value("${app.cdn.default-file:mart_default.png}")
  private String defaultFile;

  private String baseUrl() {
    return String.format("https://%s.s3.%s.amazonaws.com", bucket, region);
  }

  public String resolveBrandCode(String storeName) {
    return Brand.fromStoreName(storeName).code();
  }

  public String resolveImageUrl(String storeName) {
    Brand b = Brand.fromStoreName(storeName);
    String filename = (b == Brand.DEFAULT || b.filename() == null) ? defaultFile : b.filename();
    return String.format("%s/%s/%s", baseUrl(), marketPath, filename);
  }
}
