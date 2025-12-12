package com.spring.fit.backend.common.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spring.fit.backend.common.config.CloudinaryProperties;
import com.spring.fit.backend.common.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService implements ImageService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    // NEW: upload từ File (dùng khi ta unzip file về disk)
    @Override
    public String uploadImage(File file, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file,
                ObjectUtils.asMap("folder", folder));
        return uploadResult.get("secure_url").toString();
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", folder));
        return uploadResult.get("secure_url").toString();
    }

    @Override
    public void deleteImage(String publicId) throws IOException {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Successfully deleted image with public_id: {}", publicId);
        } catch (Exception e) {
            log.error("Error deleting image with public_id {}: {}", publicId, e.getMessage(), e);
            throw new IOException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Pattern để extract public_id từ Cloudinary URL
        // Ví dụ: https://res.cloudinary.com/your-cloud/image/upload/v1234567890/folder/image_name.jpg
        // Sẽ extract: folder/image_name
        Pattern pattern = Pattern.compile("/(?:v\\d+/)?([^/]+\\.[^.]+)$");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            String fileName = matcher.group(1);
            // Remove file extension
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }

        log.warn("Could not extract public_id from URL: {}", url);
        return null;
    }

    @Override
    public String uploadBase64Image(String base64DataUrl, String folder) throws IOException {
        // Parse base64 data URL: data:image/png;base64,iVBORw0KGgo...
        if (base64DataUrl == null || !base64DataUrl.startsWith("data:")) {
            throw new IOException("Invalid base64 data URL");
        }
        
       
        Map uploadResult = cloudinary.uploader().upload(base64DataUrl,
                ObjectUtils.asMap("folder", folder));
        return uploadResult.get("secure_url").toString();
    }
}
