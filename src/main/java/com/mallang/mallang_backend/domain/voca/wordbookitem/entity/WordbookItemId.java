package com.mallang.mallang_backend.domain.voca.wordbookitem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class WordbookItemId implements Serializable {

    @Column(name = "wordbook_id")
    private Long wordbookId;

    @Column(name = "word")
    private String word;
}