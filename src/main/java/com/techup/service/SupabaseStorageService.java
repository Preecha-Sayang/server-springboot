package com.techup.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    private final OkHttpClient client = new OkHttpClient();

    public String uploadFile(MultipartFile file, Long tripId) throws IOException {
        String fileName = generateFileName(file, tripId);
        String url = String.format("%s/storage/v1/object/%s/%s", 
            supabaseUrl, bucketName, fileName);

        RequestBody requestBody = RequestBody.create(
            file.getBytes(),
            MediaType.parse(file.getContentType())
        );

        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + supabaseKey)
            .addHeader("Content-Type", file.getContentType())
            .post(requestBody)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Upload failed: " + response.body().string());
            }
            return getPublicUrl(fileName);
        }
    }

    public void deleteFile(String photoUrl) throws IOException {
        // Extract file path from URL
        String fileName = photoUrl.substring(photoUrl.lastIndexOf(bucketName + "/") + bucketName.length() + 1);
        String url = String.format("%s/storage/v1/object/%s/%s", 
            supabaseUrl, bucketName, fileName);

        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + supabaseKey)
            .delete()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Delete failed: " + response.body().string());
            }
        }
    }

    private String generateFileName(MultipartFile file, Long tripId) {
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".") 
            ? originalName.substring(originalName.lastIndexOf(".")) 
            : "";
        return String.format("trips/%d/%s%s", tripId, UUID.randomUUID(), extension);
    }

    private String getPublicUrl(String fileName) {
        return String.format("%s/storage/v1/object/public/%s/%s", 
            supabaseUrl, bucketName, fileName);
    }
}