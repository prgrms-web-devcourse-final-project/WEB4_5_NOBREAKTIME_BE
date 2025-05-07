package com.mallang.mallang_backend.global.filter.code;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TxIdController {

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok().body("txId = " + MDC.get("transactionId"));
    }
}
