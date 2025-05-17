package com.mallang.mallang_backend.domain.plan.entity.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ImageUploadRequest {

    private String imageUrl; // 소셜 로그인 사용자 -> URL
    private MultipartFile imageFile; // 수정 시에는 단순 이미지 파일

    public ImageUploadRequest(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ImageUploadRequest(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
}
