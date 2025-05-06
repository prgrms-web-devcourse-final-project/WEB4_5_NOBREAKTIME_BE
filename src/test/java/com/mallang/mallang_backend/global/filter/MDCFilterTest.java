package com.mallang.mallang_backend.global.filter;

import com.mallang.mallang_backend.global.filter.code.TxIdController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest
@Import(TxIdController.class)
@AutoConfigureMockMvc
class MDCFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("트랜잭션 ID 검증 테스트")
    void testTransactionIdIsNotNull() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("txId = ")))
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    // "txId = " 이후의 값을 추출
                    String txId = response.replace("txId = ", "");
                    log.info("transaction Id: {}",  txId);
                    // txId가 null이거나 비어있으면 실패
                    if (txId == null || txId.isEmpty()) {
                        throw new AssertionError("transactionId is null or empty");
                    }}
                );
    }
}