package com.mallang.mallang_backend.domain.voca.wordbook.controller;

import com.mallang.mallang_backend.domain.voca.wordbook.dto.*;
import com.mallang.mallang_backend.domain.voca.wordbook.service.WordbookService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WordbookControllerTest {

    private WordbookService wordbookService;
    private WordbookController controller;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        wordbookService = mock(WordbookService.class);
        controller = new WordbookController(wordbookService);
        userDetails = mock(CustomUserDetails.class);
        when(userDetails.getMemberId()).thenReturn(1L);
    }

    @Test
    @DisplayName("단어장 단어 추가")
    void addWords() {
        AddWordToWordbookListRequest request = new AddWordToWordbookListRequest();
        ResponseEntity<RsData<Void>> response = controller.addWords(1L, request, userDetails);

        assertThat(response.getBody().getCode()).isEqualTo("200");
        verify(wordbookService).addWords(eq(1L), eq(request), eq(1L));
    }

    @Test
    @DisplayName("사용자 정의 단어 추가")
    void addWordCustom() {
        AddWordRequest request = new AddWordRequest();
        ResponseEntity<RsData<Void>> response = controller.addWordCustom(1L, request, userDetails);

        assertThat(response.getBody().getCode()).isEqualTo("200");
        verify(wordbookService).addWordCustom(eq(1L), eq(request), eq(1L));
    }

    @Test
    @DisplayName("단어장 생성")
    void createWordbook() {
        WordbookCreateRequest request = new WordbookCreateRequest();
        when(wordbookService.createWordbook(eq(request), eq(1L))).thenReturn(100L);

        ResponseEntity<RsData<Long>> response = controller.createWordbook(request, userDetails);
        assertThat(response.getBody().getData()).isEqualTo(100L);
    }

    @Test
    @DisplayName("단어장 이름 변경")
    void renameWordbook() {
        WordbookRenameRequest request = new WordbookRenameRequest();
        request.setName("updated");
        ResponseEntity<RsData<Void>> response = controller.renameWordbook(10L, request, userDetails);

        assertThat(response.getBody().getMsg()).contains("변경");
        verify(wordbookService).renameWordbook(eq(10L), eq("updated"), eq(1L));
    }

    @Test
    @DisplayName("단어장 삭제")
    void deleteWordbook() {
        ResponseEntity<RsData<Void>> response = controller.deleteWordbook(11L, userDetails);

        assertThat(response.getBody().getMsg()).contains("삭제");
        verify(wordbookService).deleteWordbook(eq(11L), eq(1L));
    }

    @Test
    @DisplayName("단어 이동")
    void moveWords() {
        WordMoveRequest request = new WordMoveRequest();
        ResponseEntity<RsData<Void>> response = controller.moveWords(request, userDetails);

        assertThat(response.getBody().getMsg()).contains("이동");
        verify(wordbookService).moveWords(eq(request), eq(1L));
    }

    @Test
    @DisplayName("단어 삭제")
    void deleteWords() {
        WordDeleteRequest request = new WordDeleteRequest();
        ResponseEntity<RsData<Void>> response = controller.deleteWords(request, userDetails);

        assertThat(response.getBody().getMsg()).contains("삭제");
        verify(wordbookService).deleteWords(eq(request), eq(1L));
    }

    @Test
    @DisplayName("단어 목록 조회")
    void getWords() {
        when(wordbookService.getWordsRandomly(eq(99L), eq(1L))).thenReturn(List.of());
        ResponseEntity<RsData<List<WordResponse>>> response = controller.getWords(99L, userDetails);

        assertThat(response.getBody().getData()).isEmpty();
    }

    @Test
    @DisplayName("단어장 목록 조회")
    void getWordbooks() {
        when(wordbookService.getWordbooks(eq(1L))).thenReturn(List.of());
        ResponseEntity<RsData<List<WordbookResponse>>> response = controller.getWordbooks(userDetails);

        assertThat(response.getBody().getCode()).isEqualTo("200");
        verify(wordbookService).getWordbooks(eq(1L));
    }

    @Test
    @DisplayName("단어장들로부터 단어 조회")
    void getWordbookItems() {
        when(wordbookService.getWordbookItems(eq(List.of(1L, 2L)), eq(1L))).thenReturn(List.of());
        ResponseEntity<RsData<List<WordResponse>>> response = controller.getWordbookItems(List.of(1L, 2L), userDetails);

        assertThat(response.getBody().getCode()).isEqualTo("200");
        verify(wordbookService).getWordbookItems(eq(List.of(1L, 2L)), eq(1L));
    }
}
