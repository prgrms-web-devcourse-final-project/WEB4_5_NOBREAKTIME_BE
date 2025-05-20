package com.mallang.mallang_backend.domain.plan.dto;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private SubscriptionType type;
    private PlanPeriod period;
    private int amount;
    private String description;
    private String title;
    private List<String> features;
    private String notice;
    private PriceInfo priceInfo;
}
