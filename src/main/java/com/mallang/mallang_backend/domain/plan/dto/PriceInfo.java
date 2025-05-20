package com.mallang.mallang_backend.domain.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceInfo {
    private int originalPrice;
    private int discountPrice;
    private int discountRate;

}