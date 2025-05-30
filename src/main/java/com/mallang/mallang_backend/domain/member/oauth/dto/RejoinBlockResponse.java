package com.mallang.mallang_backend.domain.member.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class RejoinBlockResponse {
    private int status;
    private String availableDate;
    private long daysLeft;
}