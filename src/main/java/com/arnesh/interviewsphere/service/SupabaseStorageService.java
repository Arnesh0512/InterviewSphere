package com.arnesh.interviewsphere.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.arnesh.interviewsphere.exception.CustomException;

@Service
public class SupabaseStorageService {

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public SupabaseStorageService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    
    public String uploadFile(String bucketName, MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException("Failed to upload empty file.", HttpStatus.BAD_REQUEST);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID() + extension;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + uniqueFileName;

        try {
            String contentType = file.getContentType();
            String mediaTypeString = (contentType != null) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            webClient.post()
                    .uri(uploadUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                    .header("apiKey", supabaseKey)
                    .contentType(MediaType.parseMediaType(mediaTypeString))
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + uniqueFileName;

        } catch (IOException e) {
            throw new CustomException("Failed to read file bytes: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new CustomException("Failed to upload file to Supabase Storage: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    public void deleteFile(String bucketName, String fileName) {
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        try {
            webClient.delete()
                    .uri(deleteUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                    .header("apiKey", supabaseKey)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new CustomException("Failed to delete file from Supabase Storage: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}