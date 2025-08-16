package com.likelion.picklbe.domain.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.picklbe.domain.user.dto.response.UserSummaryResponse;
import com.likelion.picklbe.domain.user.service.UserSummaryService;
import com.likelion.picklbe.global.response.BaseResponse;
import com.likelion.picklbe.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
@Tag(name = "User - My Page", description = "마이 화면 요약 정보 API")
public class UserSummaryController {

  private final UserSummaryService summaryService;

  @Operation(
      summary = "마이 요약 조회",
      description =
          """
              마이 화면에 필요한 요약 정보를 한 번에 반환합니다.
              - `daysSinceFriend`: 피클이와 친해진 지 N일째 (KST 기준, **당일 포함**)
              - 포인트 합계 및 찜/히스토리 개수 포함
              """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BaseResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "성공 예시",
                          value =
                              """
                                  {
                                    "success": true,
                                    "code": 200,
                                    "message": "요약 조회에 성공했습니다.",
                                    "data": {
                                      "nickname": "정시태근희망러",
                                      "region": "성북구 정릉동",
                                      "points": 30000,
                                      "daysSinceFriend": 23,
                                      "favoriteIngredientCount": 20,
                                      "favoriteRecipeCount": 4,
                                      "pickleHistoryCount": 10
                                    }
                                  }
                                  """)
                    }))
      })
  @GetMapping("/summary")
  public BaseResponse<UserSummaryResponse> summary(
      @Parameter(hidden = true) // Swagger 문서에서 principal 파라미터 숨김
          @AuthenticationPrincipal
          CustomUserDetails me) {
    return BaseResponse.success("요약 조회에 성공했습니다.", summaryService.get(me.getId()));
  }
}
