package com.mallang.mallang_backend.domain.payment.dto.approve;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Failure {
    private String code;
    private String message;
}