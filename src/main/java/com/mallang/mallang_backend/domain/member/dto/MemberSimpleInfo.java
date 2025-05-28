package com.mallang.mallang_backend.domain.member.dto;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.global.common.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MemberSimpleInfo {

    private Long id;
    private SubscriptionType type;
    private Language language;
}
