package com.techup.service;

import com.techup.dto.TripRequest;
import com.techup.dto.TripResponse;
import com.techup.entity.Trip;
import com.techup.entity.User;
import com.techup.repository.TripRepository;
import com.techup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final SupabaseStorageService storageService;

    private static final int MAX_PHOTOS = 5;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    // เพิ่มรูปภาพเข้าทริป
    @Transactional
    public TripResponse uploadPhotos(Long tripId, List<MultipartFile> files, Long userId) throws IOException {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new RuntimeException("ไม่พบทริป"));

        // ตรวจสอบสิทธิ์
        if (!trip.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("คุณไม่มีสิทธิ์แก้ไขทริปนี้");
        }

        // นับรูปปัจจุบัน
        int currentPhotoCount = trip.getPhotos() != null ? trip.getPhotos().length : 0;
        
        if (currentPhotoCount + files.size() > MAX_PHOTOS) {
            throw new IllegalArgumentException(
                String.format("สามารถอัพโหลดได้สูงสุด %d รูปต่อทริป (ปัจจุบันมี %d รูป)", 
                MAX_PHOTOS, currentPhotoCount)
            );
        }

        List<String> newPhotoUrls = new ArrayList<>();

        // Upload แต่ละไฟล์
        for (MultipartFile file : files) {
            validateFile(file);
            String photoUrl = storageService.uploadFile(file, tripId);
            newPhotoUrls.add(photoUrl);
        }

        // เพิ่ม URL เข้า array เดิม
        List<String> allPhotos = new ArrayList<>();
        if (trip.getPhotos() != null) {
            allPhotos.addAll(Arrays.asList(trip.getPhotos()));
        }
        allPhotos.addAll(newPhotoUrls);
        
        trip.setPhotos(allPhotos.toArray(new String[0]));
        Trip savedTrip = tripRepository.save(trip);

        return convertToResponse(savedTrip);
    }

    // ลบรูปภาพจากทริป
    @Transactional
    public TripResponse deletePhoto(Long tripId, String photoUrl, Long userId) throws IOException {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new RuntimeException("ไม่พบทริป"));

        // ตรวจสอบสิทธิ์
        if (!trip.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("คุณไม่มีสิทธิ์แก้ไขทริปนี้");
        }

        if (trip.getPhotos() == null || trip.getPhotos().length == 0) {
            throw new RuntimeException("ไม่มีรูปภาพในทริปนี้");
        }

        // ตรวจสอบว่ามี URL นี้อยู่จริง
        List<String> photos = new ArrayList<>(Arrays.asList(trip.getPhotos()));
        if (!photos.contains(photoUrl)) {
            throw new RuntimeException("ไม่พบรูปภาพนี้ในทริป");
        }

        // ลบไฟล์จาก Supabase Storage
        try {
            storageService.deleteFile(photoUrl);
        } catch (IOException e) {
            // Log error แต่ยังคงลบ URL ออกจาก database
            System.err.println("Failed to delete file from storage: " + e.getMessage());
        }

        // ลบ URL ออกจาก array
        photos.remove(photoUrl);
        trip.setPhotos(photos.toArray(new String[0]));
        Trip savedTrip = tripRepository.save(trip);

        return convertToResponse(savedTrip);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ไฟล์ว่างเปล่า");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("ไฟล์มีขนาดใหญ่เกินไป (สูงสุด %d MB)", MAX_FILE_SIZE / 1024 / 1024)
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "รองรับเฉพาะไฟล์ประเภท: " + String.join(", ", ALLOWED_TYPES)
            );
        }
    }

    // Methods อื่นๆ ที่มีอยู่แล้ว...
    public List<TripResponse> getAllTrips() {
        return tripRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ไม่พบทริป"));
        return convertToResponse(trip);
    }

    public List<TripResponse> searchByKeyword(String keyword) {
        return tripRepository.searchByKeyword(keyword).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public TripResponse createTrip(TripRequest request) {
        User author = userRepository.findById(request.getAuthorId())
            .orElseThrow(() -> new RuntimeException("ไม่พบผู้ใช้"));

        Trip trip = Trip.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .photos(request.getPhotos() != null ? request.getPhotos().toArray(new String[0]) : null)  // ← แปลง List → Array
            .tags(request.getTags() != null ? request.getTags().toArray(new String[0]) : null)  // ← แปลง List → Array
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .author(author)
            .build();

        Trip savedTrip = tripRepository.save(trip);
        return convertToResponse(savedTrip);
    }

    @Transactional
    public TripResponse updateTrip(Long id, TripRequest request, Long userId) {
        Trip trip = tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ไม่พบทริป"));

        if (!trip.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("คุณไม่มีสิทธิ์แก้ไขทริปนี้");
        }

        trip.setTitle(request.getTitle());
        trip.setDescription(request.getDescription());
        trip.setTags(request.getTags() != null ? request.getTags().toArray(new String[0]) : null);  // ← แปลง List → Array
        trip.setLatitude(request.getLatitude());
        trip.setLongitude(request.getLongitude());

        Trip savedTrip = tripRepository.save(trip);
        return convertToResponse(savedTrip);
    }


    @Transactional
    public void deleteTrip(Long id, Long userId) {
        Trip trip = tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ไม่พบทริป"));

        if (!trip.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("คุณไม่มีสิทธิ์ลบทริปนี้");
        }

        // ลบรูปภาพทั้งหมดจาก Storage ก่อน
        if (trip.getPhotos() != null) {
            for (String photoUrl : trip.getPhotos()) {
                try {
                    storageService.deleteFile(photoUrl);
                } catch (IOException e) {
                    System.err.println("Failed to delete photo: " + photoUrl);
                }
            }
        }

        tripRepository.delete(trip);
    }

    private TripResponse convertToResponse(Trip trip) {
        return TripResponse.builder()
            .id(trip.getId())
            .title(trip.getTitle())
            .description(trip.getDescription())
            .photos(trip.getPhotos())
            .tags(trip.getTags())
            .latitude(trip.getLatitude())
            .longitude(trip.getLongitude())
            .authorId(trip.getAuthor().getId())
            .createdAt(trip.getCreatedAt().atOffset(java.time.ZoneOffset.UTC))
            .updatedAt(trip.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC))
            .build();
    }
}