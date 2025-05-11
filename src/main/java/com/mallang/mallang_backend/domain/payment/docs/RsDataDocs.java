package com.mallang.mallang_backend.domain.payment.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "공통 응답 Wrapper (문서화용)")
public abstract class RsDataDocs {
    @Schema(description = "응답 코드", example = "200")
    protected String code;

    @Schema(description = "응답 메시지", example = "성공")
    protected String msg;
}