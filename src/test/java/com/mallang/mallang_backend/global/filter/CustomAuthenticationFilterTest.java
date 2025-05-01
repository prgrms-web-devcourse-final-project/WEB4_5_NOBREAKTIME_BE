package com.mallang.mallang_backend.global.filter;

import com.mallang.mallang_backend.MallangBackendApplication;
import com.mallang.mallang_backend.global.token.JwtService;
import com.mallang.mallang_backend.global.token.TokenPair;
import com.mallang.mallang_backend.global.token.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

import static com.mallang.mallang_backend.global.constants.AppConstants.REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("local")
@SpringBootTest(classes = {
        MallangBackendApplication.class,
        CustomAuthenticationFilterTest.TestController.class
})
@AutoConfigureMockMvc
class CustomAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtService jwtService;

    private TokenPair validTokenPair;
    private String expiredAccessToken;

    @BeforeEach
    void setUp() {
        // 테스트용 토큰 생성
        validTokenPair = tokenService.createTokenPair(1L, "ROLE_USER");
        expiredAccessToken = jwtService.createToken(
                Map.of("memberId", 1L, "role", "ROLE_USER"),
                1000L // 1초를 유효기간으로 만료 토큰 생성
        );
    }

    // 테스트용 컨트롤러
    @RestController
    @RequestMapping("/test")
    static class TestController {
        @GetMapping("/auth")
        public Map<String, Object> authTest() {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            return Map.of(
                    "memberId", userDetails.getMemberId(),
                    "roleName", userDetails.getRoleName()
            );
        }
    }

    @Test
    @DisplayName("유효한 액세스 토큰으로 인증 성공")
    void validAccessToken() throws Exception {
        mockMvc.perform(get("/test/auth")
                        .header("Authorization", "Bearer " + validTokenPair.getAccessToken())
                        .cookie(new Cookie("refreshToken", validTokenPair.getRefreshToken())))
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.roleName").value("ROLE_USER"));
    }

    @Test
    @DisplayName("만료된 액세스 토큰으로 리프레시 토큰 사용")
    void expiredAccessTokenWithValidRefreshToken() throws Exception {
        Thread.sleep(3000L);
        MvcResult result = mockMvc.perform(get("/test/auth")
                        .header("Authorization", "Bearer " + expiredAccessToken)
                        .cookie(new Cookie(REFRESH_TOKEN, validTokenPair.getRefreshToken())))
                .andExpect(status().isOk())
                .andReturn();

        String newAccessToken = result.getResponse()
                .getHeader("Authorization")
                .replace("Bearer ", "");

        assertTrue(jwtService.isValidToken(newAccessToken));
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 요청 시 500 에러")
    void invalidRefreshToken() throws Exception {
        Thread.sleep(3000L);
        mockMvc.perform(get("/test/auth")
                        .header("Authorization", "Bearer " + expiredAccessToken)
                        .cookie(new Cookie(REFRESH_TOKEN, "invalid_token")))
                .andExpect(status().isInternalServerError());
    }
}