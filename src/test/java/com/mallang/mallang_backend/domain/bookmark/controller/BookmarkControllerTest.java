package com.mallang.mallang_backend.domain.bookmark.controller;

import com.mallang.mallang_backend.domain.bookmark.service.BookmarkService;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("local")
@AutoConfigureMockMvc(addFilters = false)
@Import(BookmarkControllerTest.TestConfig.class)
@SpringBootTest
@DisplayName("BookmarkController 통합 테스트")
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookmarkService bookmarkService;

    private CustomUserDetails user;

    @BeforeEach
    void setup() {
        user = createTestUser();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private CustomUserDetails createTestUser() {
        return new CustomUserDetails(1L, "ROLE_STANDARD");
    }

    @Test
    @DisplayName("영상 북마크 추가")
    void addBookmark() throws Exception {
        doNothing().when(bookmarkService).addBookmark(any(), any());

        mockMvc.perform(post("/api/v1/bookmarks/video123")
                        .requestAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.msg").value("북마크 추가 완료"));
    }

    @Test
    @DisplayName("영상 북마크 제거")
    void removeBookmark() throws Exception {
        doNothing().when(bookmarkService).removeBookmark(any(), any());

        mockMvc.perform(delete("/api/v1/bookmarks/video123")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("북마크 제거 완료"));
    }

    @Test
    @DisplayName("북마크 영상 전체 조회")
    void getAllBookmarks() throws Exception {
        Videos video1 = Videos.builder()
                .id("video123")
                .videoTitle("Title1")
                .thumbnailImageUrl("thumb1.jpg")
                .channelTitle("채널1")
                .language(Language.ENGLISH)
                .build();

        Videos video2 = Videos.builder()
                .id("video456")
                .videoTitle("Title2")
                .thumbnailImageUrl("thumb2.jpg")
                .channelTitle("채널2")
                .language(Language.ENGLISH)
                .build();

        when(bookmarkService.getBookmarks(any())).thenReturn(List.of(video1, video2));

        mockMvc.perform(get("/api/v1/bookmarks")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("북마크 목록 조회 성공"))
                .andExpect(jsonPath("$.data.length()", org.hamcrest.Matchers.is(2)));
    }

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Bean
        public BookmarkService bookmarkService() {
            return mock(BookmarkService.class);
        }
    }
}
