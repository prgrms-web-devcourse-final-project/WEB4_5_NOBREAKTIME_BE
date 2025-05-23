package com.mallang.mallang_backend.domain.voca.word.controller;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;
import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse.WordMeaning;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WordControllerTest {

    @Mock
    private WordService wordService;

    @InjectMocks
    private WordController wordController;

    private MockMvc mockMvc;

    private final CustomUserDetails userDetails = new CustomUserDetails(1L, "ROLE_STANDARD");

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver loginResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                return parameter.getParameterType().equals(CustomUserDetails.class);
            }

            @Override
            public Object resolveArgument(org.springframework.core.MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                return userDetails;
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(wordController)
                .setCustomArgumentResolvers(loginResolver)
                .build();
    }

    @Test
    @DisplayName("단어 검색 성공시 결과 반환")
    void searchWord_success() throws Exception {
        String word = "light";
        WordMeaning meaning = new WordMeaning();
        meaning.setPartOfSpeech("형용사");
        meaning.setMeaning("가벼운");
        meaning.setDifficulty(1);

        WordSearchResponse response = new WordSearchResponse(List.of(meaning));

        when(wordService.searchWord(word, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/words/search")
                        .param("word", word)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value(word + "에 대한 조회 결과입니다."))
                .andExpect(jsonPath("$.data.meanings[0].meaning").value("가벼운"));
    }
}