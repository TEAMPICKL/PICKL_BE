package com.likelion.picklbe.global.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class S3Config {

  private AWSCredentials awsCredentials;

  @Value("${cloud.aws.credentials.access-key}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secret-key}")
  private String secretKey;

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  // 1~8월 폴더만 유지
  @Value("${cloud.aws.s3.path.m01}")
  private String m01Folder;

  @Value("${cloud.aws.s3.path.m02}")
  private String m02Folder;

  @Value("${cloud.aws.s3.path.m03}")
  private String m03Folder;

  @Value("${cloud.aws.s3.path.m04}")
  private String m04Folder;

  @Value("${cloud.aws.s3.path.m05}")
  private String m05Folder;

  @Value("${cloud.aws.s3.path.m06}")
  private String m06Folder;

  @Value("${cloud.aws.s3.path.m07}")
  private String m07Folder;

  @Value("${cloud.aws.s3.path.m08}")
  private String m08Folder;

  @Value("${cloud.aws.s3.path.market}")
  private String MARKETFolder;

  @PostConstruct
  public void init() {
    this.awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
  }

  @Bean
  public AmazonS3 amazonS3() {
    AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
    return AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
        .build();
  }

  @Bean
  public AWSCredentialsProvider awsCredentialsProvider() {
    return new AWSStaticCredentialsProvider(awsCredentials);
  }
}
