package com.likelion.picklbe.global.s3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.brand.Brand;

import com.amazonaws.services.s3.AmazonS3;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandImagePromotionService {

  private final AmazonS3 s3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.s3.path.brand:images/brand}")
  private String brandPath;

  /** UUID 키를 브랜드 고정 파일명으로 승격 */
  public String promote(String uuidKey, Brand brand) {
    if (brand == null || brand.filename() == null) {
      throw new IllegalArgumentException("고정 파일명이 없는 브랜드입니다: " + brand);
    }
    String srcKey = trimSlash(brandPath) + "/" + trimSlash(uuidKey);
    String dstKey = trimSlash(brandPath) + "/" + brand.filename();

    s3.copyObject(bucket, srcKey, bucket, dstKey);
    s3.deleteObject(bucket, srcKey);

    return dstKey; // e.g. images/brand/emart.png
  }

  private String trimSlash(String s) {
    return s == null ? "" : s.replaceAll("^/+", "").replaceAll("/+$", "");
  }
}
