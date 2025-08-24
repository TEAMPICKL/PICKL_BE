package com.likelion.picklbe.global.s3.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.picklbe.domain.brand.Brand;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandImageUploadService {

  private final AmazonS3 s3;
  private final BrandImagePromotionService promotionService;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.s3.path.brand:images/brand}")
  private String brandPath;

  @Value("${app.cdn.base-url:https://picklocal.s3.ap-northeast-2.amazonaws.com}")
  private String baseUrl;

  public String uploadAndMaybePromote(MultipartFile file, String brandCode) {
    // 1) UUID로 임시 업로드
    String uuid = UUID.randomUUID().toString();
    String tempKey = trimSlash(brandPath) + "/" + uuid;

    try (InputStream is = file.getInputStream()) {
      ObjectMetadata meta = new ObjectMetadata();
      meta.setContentLength(file.getSize());
      meta.setContentType(file.getContentType());
      s3.putObject(bucket, tempKey, is, meta);
      // 필요시: 퍼블릭 권한/캐시 헤더 지정
    } catch (Exception e) {
      throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
    }

    // 2) brandCode 있으면 고정 파일명으로 승격
    if (brandCode != null && !brandCode.isBlank()) {
      Brand brand = Brand.fromCodeSafe(brandCode);
      String fixedKey = promotionService.promote(uuid, brand);
      return toUrl(fixedKey);
    }

    // brandCode 없으면 임시 UUID URL 반환 (나중에 관리자 프로모트로 정리)
    return toUrl(tempKey);
  }

  private String toUrl(String key) {
    return rtrim(baseUrl) + "/" + key.replaceAll("^/+", "");
  }

  private String trimSlash(String s) {
    return s == null ? "" : s.replaceAll("^/+", "").replaceAll("/+$", "");
  }

  private String rtrim(String s) {
    return (s == null) ? "" : s.replaceAll("/+$", "");
  }
}
