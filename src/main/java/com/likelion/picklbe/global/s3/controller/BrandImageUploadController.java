package com.likelion.picklbe.global.s3.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.picklbe.global.s3.service.BrandImageUploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
@Tag(name = "S3", description = "S3 업로드/관리 API")
public class BrandImageUploadController {

  private final BrandImageUploadService uploadService;

  @PostMapping(
      path = "/brand-image",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "브랜드 이미지 업로드 (옵션: 즉시 승격)",
      description =
          """
          multipart/form-data 업로드. brandCode를 주면 즉시 승격하여 고정 URL을 반환합니다.
          brandCode 예: emart, homeplus, lotte-mart, emart-everyday, no-brand, costco, lotte-super, lotte-fresh, traders, hanaro
          """)
  @ApiResponse(
      responseCode = "200",
      description = "업로드 성공",
      content = @Content(schema = @Schema(implementation = SimpleUrl.class)))
  public ResponseEntity<SimpleUrl> uploadBrandImage(
      @Parameter(description = "브랜드 코드(선택)", example = "emart")
          @RequestParam(value = "brandCode", required = false)
          String brandCode,
      @Parameter(description = "업로드할 이미지 파일") @RequestPart("file") MultipartFile file) {
    String url = uploadService.uploadAndMaybePromote(file, brandCode);
    return ResponseEntity.ok(new SimpleUrl(url));
  }

  public record SimpleUrl(String url) {}
}
