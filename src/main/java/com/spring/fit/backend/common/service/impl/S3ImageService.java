package com.spring.fit.backend.common.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.common.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class S3ImageService implements ImageService {

    private static final Set<String> ALLOWED_IMAGE_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L; // 10MB

    private final S3Client s3Client;
    private final Tika tika = new Tika();

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.region:${aws.region:ap-southeast-1}}")
    private String region;

    @Value("${aws.s3.cloudfront-domain:}")
    private String cloudFrontDomain;

    @Override
    public String uploadImage(File file, String folder) throws IOException {
        if (file == null || !file.exists()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "File ảnh không tồn tại!");
        }

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String contentType = validateAndDetectImageMimeType(fileBytes, file.getName());

        String key = generateS3Key(folder, file.getName());
        uploadToS3(key, RequestBody.fromBytes(fileBytes), contentType);
        return getPublicUrl(key);
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "File ảnh tải lên rỗng!");
        }

        byte[] fileBytes = file.getBytes();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        String contentType = validateAndDetectImageMimeType(fileBytes, originalFilename);

        String key = generateS3Key(folder, originalFilename);
        uploadToS3(key, RequestBody.fromBytes(fileBytes), contentType);
        return getPublicUrl(key);
    }

    @Override
    public String uploadBase64Image(String base64DataUrl, String folder) throws IOException {
        if (base64DataUrl == null || !base64DataUrl.startsWith("data:")) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Định dạng Base64 data URL không hợp lệ!");
        }

        // Tách mimeType và raw base64 data (vd: data:image/png;base64,iVBOR...)
        String[] parts = base64DataUrl.split(",");
        if (parts.length < 2) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Dữ liệu ảnh Base64 không hợp lệ!");
        }
        String base64Data = parts[1];

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        String detectedMime = validateAndDetectImageMimeType(imageBytes, "base64_upload");

        String extension = detectedMime.contains("/") ? detectedMime.substring(detectedMime.indexOf("/") + 1) : "jpg";
        String key = folder + "/" + UUID.randomUUID() + "." + extension;

        uploadToS3(key, RequestBody.fromBytes(imageBytes), detectedMime);
        return getPublicUrl(key);
    }

    /**
     * Quét và kiểm tra Magic Bytes (File Signature) bằng Apache Tika để ngăn chặn
     * các tập tin độc hại, mã thực thi (.exe, .php, script) giả mạo đuôi ảnh.
     */
    private String validateAndDetectImageMimeType(byte[] bytes, String filename) {
        if (bytes == null || bytes.length == 0) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Dữ liệu file ảnh rỗng!");
        }

        if (bytes.length > MAX_FILE_SIZE_BYTES) {
            throw new ErrorException(HttpStatus.BAD_REQUEST,
                    "Kích thước file ảnh vượt quá giới hạn tối đa cho phép (10MB)!");
        }

        // Phân tích sâu cấu trúc nhị phân của file (Magic Bytes)
        String detectedMimeType = tika.detect(bytes, filename);
        log.debug("Apache Tika Magic Bytes detection for [{}]: detected={}", filename, detectedMimeType);

        if (!ALLOWED_IMAGE_MIME_TYPES.contains(detectedMimeType)) {
            log.warn("CẢNH BÁO BẢO MẬT: File [{}] không vượt qua kiểm tra Magic Bytes! Loại phát hiện: {}",
                    filename, detectedMimeType);
            throw new ErrorException(HttpStatus.BAD_REQUEST,
                    "Tệp tin không phải là hình ảnh hợp lệ hoặc bị nghi ngờ chứa mã độc/giả mạo định dạng (MIME: " + detectedMimeType + ")!");
        }

        return detectedMimeType;
    }

    @Override
    public void deleteImage(String publicIdOrKey) throws IOException {
        if (!StringUtils.hasText(publicIdOrKey)) {
            log.debug("deleteImage called with null or empty key, skipping.");
            return;
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(publicIdOrKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("Successfully deleted S3 object with key: {}", publicIdOrKey);
        } catch (Exception e) {
            log.error("Error deleting image from S3 with key [{}]: {}", publicIdOrKey, e.getMessage(), e);
            throw new IOException("Failed to delete image from S3: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractPublicIdFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        String trimmedUrl = url.trim();

        // 1. Kiểm tra CloudFront Domain (nếu đã cấu hình custom domain hoặc dạng dxxxx.cloudfront.net)
        if (StringUtils.hasText(cloudFrontDomain)) {
            String cleanDomain = cloudFrontDomain.trim().replaceFirst("^https?://", "");
            if (cleanDomain.endsWith("/")) {
                cleanDomain = cleanDomain.substring(0, cleanDomain.length() - 1);
            }
            int cfIndex = trimmedUrl.indexOf(cleanDomain + "/");
            if (cfIndex != -1) {
                return trimmedUrl.substring(cfIndex + cleanDomain.length() + 1);
            }
        }

        // Kiểm tra domain CloudFront chung dạng *.cloudfront.net/<key>
        int genericCfIndex = trimmedUrl.indexOf(".cloudfront.net/");
        if (genericCfIndex != -1) {
            return trimmedUrl.substring(genericCfIndex + ".cloudfront.net/".length());
        }

        // 2. Kiểm tra URL S3 tiêu chuẩn (https://<bucket>.s3.<region>.amazonaws.com/<key>)
        String domainSuffix = ".amazonaws.com/";
        int index = trimmedUrl.indexOf(domainSuffix);
        if (index != -1) {
            String path = trimmedUrl.substring(index + domainSuffix.length());
            if (path.startsWith(bucketName + "/")) {
                return path.substring(bucketName.length() + 1);
            }
            return path;
        }

        // 3. Nếu là link bên ngoài (Unsplash, Google, Cloudinary cũ...), không thuộc S3/CloudFront này
        log.debug("URL [{}] is not hosted on our S3/CloudFront, returning null", url);
        return null;
    }

    private void uploadToS3(String key, RequestBody requestBody, String contentType) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putRequest, requestBody);
        log.info("Uploaded image to S3: bucket={}, key={}", bucketName, key);
    }

    private String generateS3Key(String folder, String originalFilename) {
        String sanitizedFilename = originalFilename.replaceAll("\\s+", "_");
        return folder + "/" + UUID.randomUUID() + "_" + sanitizedFilename;
    }

    private String getPublicUrl(String key) {
        if (StringUtils.hasText(cloudFrontDomain)) {
            String cleanDomain = cloudFrontDomain.trim();
            if (cleanDomain.endsWith("/")) {
                cleanDomain = cleanDomain.substring(0, cleanDomain.length() - 1);
            }
            if (!cleanDomain.startsWith("http://") && !cleanDomain.startsWith("https://")) {
                cleanDomain = "https://" + cleanDomain;
            }
            return cleanDomain + "/" + key;
        }
        String effectiveRegion = (region != null && !region.isBlank() && !"ap-southeast1".equalsIgnoreCase(region.trim())) 
                ? region.trim() : "ap-southeast-1";
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, effectiveRegion, key);
    }
}
