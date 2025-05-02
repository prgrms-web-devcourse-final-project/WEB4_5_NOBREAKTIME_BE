package com.mallang.mallang_backend.global.util.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.mallang.mallang_backend.domain.member.dto.ImageUploadRequest;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static com.mallang.mallang_backend.global.config.S3Config.bucket;
import static com.mallang.mallang_backend.global.constants.AppConstants.IMAGE_PREFIX_KEY;
import static com.mallang.mallang_backend.global.constants.AppConstants.IMAGE_TYPE_KEY;
import static com.mallang.mallang_backend.global.exception.ErrorCode.FILE_UPLOAD_FAILED;
import static com.mallang.mallang_backend.global.exception.ErrorCode.NOT_EXIST_BUCKET;

/**
 * S3에 이미지를 업로드하는 유틸리티 클래스
 */
@Slf4j
@Component
@Transactional(readOnly = true)
public class S3ImageUploader {

    private static AmazonS3 amazonS3;

    public S3ImageUploader(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    /**
     * 외부 이미지 URL or 파일을 받아 S3에 업로드하고, 업로드된 이미지의 S3 URL을 반환
     * URL 예시: s3://버킷_이름/원본_이미지_이름.jpg
     *
     * @param request 업로드할 이미지의 외부 URL or 단순 이미지 파일
     * @return 업로드된 이미지의 S3 URL
     * @throws IOException 이미지 다운로드 또는 S3 업로드 중 예외 발생 시 -> 오류 핸들러 처리
     */
    public String uploadImageURL(ImageUploadRequest request) {
        String key;
        try (InputStream inputStream = request.getImageUrl() != null
                ? new URL(request.getImageUrl()).openStream()
                : request.getImageFile().getInputStream()) {

            String fileName = UUID.randomUUID().toString() +
                    (request.getImageFile() != null
                            ? getFileExtension(request.getImageFile().getOriginalFilename())
                            : ".jpg");
            key = IMAGE_PREFIX_KEY + "/" + fileName; // S3에 저장할 전체 key를 생성

            ObjectMetadata metadata = createImageMetadata();
            if (request.getImageFile() != null) {
                metadata.setContentLength(request.getImageFile().getSize());
                metadata.setContentType(request.getImageFile().getContentType());
            }
            amazonS3.putObject(bucket, key, inputStream, metadata);
        } catch (IOException e) {
            throw new ServiceException(FILE_UPLOAD_FAILED, e);
        }

        log.debug("s3에 프로필 이미지 업로드 성공: {}", amazonS3.getUrl(bucket, key).toString());
        return amazonS3.getUrl(bucket, key).toString();
    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return ".jpg"; // 기본값
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex != -1) ? fileName.substring(dotIndex) : ".jpg";
    }

    /**
     * 이미지 업로드에 사용할 ObjectMetadata 를 생성
     *
     * @return ObjectMetadata
     */
    private ObjectMetadata createImageMetadata() {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(IMAGE_TYPE_KEY);
        return metadata;
    }

    /**
     * S3 프로필 이미지 URL 을 통해 해당 S3 객체를 삭제
     *
     * @param s3ProfileImageUrl 삭제할 S3 객체의 전체 URL 예: https://버킷이름.s3.ap-northeast-2.amazonaws.com/원본파일이름.jpg
     * @throws ServiceException 버킷이 존재하지 않거나 삭제에 실패할 경우 발생
     */
    public void deleteObjectByUrl(String s3ProfileImageUrl) {
        AmazonS3URI s3Uri = new AmazonS3URI(s3ProfileImageUrl);

        String bucketName = s3Uri.getBucket();
        String objectKey = s3Uri.getKey();

        validateBucketExists(bucketName);

        amazonS3.deleteObject(bucketName, objectKey);
        log.debug("S3에서 프로필 사진 삭제 성공, bucketName: {}, key: {}", bucketName, objectKey);
    }

    /**
     * 지정한 S3 버킷이 존재하는지 검증
     *
     * @param bucketName 검증할 S3 버킷 이름
     * @throws ServiceException 버킷이 존재하지 않을 경우 발생
     */
    private void validateBucketExists(String bucketName) {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            throw new ServiceException(NOT_EXIST_BUCKET);
        }
    }
}
