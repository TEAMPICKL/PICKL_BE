package com.likelion.picklbe.domain.brand.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.likelion.picklbe.domain.brand.Brand;
import com.likelion.picklbe.global.s3.service.BrandImagePromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/brands")
@Tag(name = "Brand Admin", description = "브랜드 이미지 승격(관리자) API")
public class BrandAdminController {

  private final BrandImagePromotionService promotionService;

  /** 단건 승격: uuid, brandCode */
  @PostMapping(path = "/promote", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "브랜드 이미지 단건 승격",
      description =
          """
          업로드된 UUID 키 파일을 해당 브랜드의 **고정 파일명**으로 복사(승격)합니다.
          - `uuid`는 `images/brand/<uuid>`에서 `<uuid>` **파일명 부분**만 넣으세요.
          - 가능한 brandCode 예: emart, homeplus, lotte-mart, emart-everyday, no-brand, costco, lotte-super, lotte-fresh, traders, hanaro
          반환값은 최종 S3 키(예: `images/brand/emart.png`)입니다.
          """)
  @ApiResponse(responseCode = "200", description = "승격 성공 (텍스트 키 반환)")
  @ApiResponse(responseCode = "400", description = "유효하지 않은 brandCode", content = @Content)
  public ResponseEntity<String> promote(
      @Parameter(
              description = "UUID 파일명 (예: 8799915c-9018-406a-...)",
              example = "8799915c-9018-406a-ad2e-47678f87ca96")
          @RequestParam("uuid")
          String uuidKey,
      @Parameter(description = "브랜드 코드 (예: emart, homeplus, lotte-mart)", example = "emart")
          @RequestParam("brandCode")
          String brandCode) {
    Brand brand = Brand.fromCodeSafe(brandCode);
    if (brand == Brand.DEFAULT || brand.filename() == null) {
      return ResponseEntity.badRequest().body("유효하지 않은 brandCode: " + brandCode);
    }
    String dstKey = promotionService.promote(uuidKey, brand);
    return ResponseEntity.ok(dstKey);
  }

  /** 배치 승격: [{uuid, brandCode}, ...] */
  @PostMapping(
      path = "/promote-batch",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "브랜드 이미지 배치 승격",
      description =
          """
          여러 개의 UUID 파일을 한 번에 승격합니다.
          요청 바디 예:
          [
            {"uuid":"8799915c-9018-406a-ad2e-47678f87ca96","brandCode":"emart"},
            {"uuid":"b45a7445-be38-4573-89dc-c5afbebcb0e9","brandCode":"homeplus"}
          ]
          반환값은 최종 S3 키 배열입니다.
          """)
  @ApiResponse(
      responseCode = "200",
      description = "승격 성공",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
  public ResponseEntity<List<String>> promoteBatch(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = PromoteReq[].class)))
          @RequestBody
          List<PromoteReq> reqs) {
    List<String> results =
        reqs.stream()
            .map(
                req -> {
                  Brand brand = Brand.fromCodeSafe(req.brandCode());
                  if (brand == Brand.DEFAULT || brand.filename() == null) {
                    throw new IllegalArgumentException("invalid brandCode: " + req.brandCode());
                  }
                  return promotionService.promote(req.uuid(), brand);
                })
            .toList();
    return ResponseEntity.ok(results);
  }

  @Schema(name = "PromoteReq", description = "승격 요청 항목")
  public record PromoteReq(
      @Schema(description = "UUID 파일명", example = "8799915c-9018-406a-ad2e-47678f87ca96")
          String uuid,
      @Schema(description = "브랜드 코드", example = "emart") String brandCode) {}
}
