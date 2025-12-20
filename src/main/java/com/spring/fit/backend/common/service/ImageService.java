package com.spring.fit.backend.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface ImageService {



    String uploadImage(File file, String folder) throws IOException;

    String uploadImage(MultipartFile file, String folder) throws IOException;
    
    String uploadBase64Image(String base64DataUrl, String folder) throws IOException;
    
    void deleteImage(String publicId) throws IOException;
    String extractPublicIdFromUrl(String url);
}
