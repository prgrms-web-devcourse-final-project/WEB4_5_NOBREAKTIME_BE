package com.mallang.mallang_backend.domain.plan.dto;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "구독 플랜 응답 정보")
public class PlanResponse {

    @Schema(description = "구독 타입", example = "STANDARD")
    private SubscriptionType type;

    @Schema(description = "플랜 기간", example = "MONTHLY")
    private PlanPeriod period;

    @Schema(description = "가격(원)", example = "4500")
    private int amount;

    @Schema(description = "플랜 설명", example = "스탠다드 정기 구독")
    private String description;

    @Schema(description = "플랜 제목")
    private String title;

    @Schema(description = "제공 기능 목록")
    private List<String> features;

    @Schema(description = "유의사항")
    private String notice;

    @Schema(description = "가격 상세 정보")
    private PriceInfo priceInfo;
}
