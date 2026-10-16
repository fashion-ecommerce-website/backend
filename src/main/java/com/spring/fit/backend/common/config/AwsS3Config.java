package com.spring.fit.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsS3Config {

    @Value("${aws.s3.region:${aws.region:ap-southeast-1}}")
    private String region;

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        String effectiveRegion = (region != null && !region.isBlank()) ? region.trim() : "ap-southeast-1";
        if ("ap-southeast1".equalsIgnoreCase(effectiveRegion)) {
            effectiveRegion = "ap-southeast-1";
        }
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3Client.builder()
                .region(Region.of(effectiveRegion))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
