package com.mallang.mallang_backend.domain.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "가격 상세 정보")
public class PriceInfo {

    @Schema(description = "정가(원)")
    private int originalPrice;

    @Schema(description = "할인가(원)")
    private int discountPrice;

    @Schema(description = "할인율(%)", example = "0.2")
    private int discountRate;
}