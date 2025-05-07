package com.mallang.mallang_backend.domain.test;

import com.mallang.mallang_backend.global.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Profile("local")
@SpringBootTest
@Import({ SecurityConfig.class, AuthTestController.class })
@AutoConfigureMockMvc
public class AuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void t1() throws Exception {
        //given
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/test"))
                .andExpect(status().is4xxClientError())
                .andReturn();
        //when
        log.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(roles = "STANDARD") // STANDARD 역할 부여
    void t2() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/test"))
                .andExpect(status().isOk());
    }
}
