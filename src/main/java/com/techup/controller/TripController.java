package com.techup.controller;

import com.techup.dto.TripRequest;
import com.techup.dto.TripResponse;
import com.techup.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // ----------------- Visitor APIs -----------------
    @GetMapping
    public List<TripResponse> getAllTrips() {
        return tripService.getAllTrips();
    }

    @GetMapping("/search")
    public List<TripResponse> searchTrips(@RequestParam String keyword) {
        return tripService.searchByKeyword(keyword);
    }

    @GetMapping("/{id}")
    public TripResponse getTripDetail(@PathVariable Long id) {
        return tripService.getTripById(id);
    }

    // ----------------- Authenticated User APIs -----------------
private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
        return null;
    }

    // สมมติเก็บ userId ไว้ใน details ของ authToken
    Object details = auth.getDetails();
    if (details instanceof Long) {
        return (Long) details;
    }

    // หรือถ้าใช้ CustomUserDetails:
    // if (auth.getPrincipal() instanceof CustomUserDetails) {
    //     return ((CustomUserDetails) auth.getPrincipal()).getUserId();
    // }

    return null;
}

    @PostMapping
    public TripResponse createTrip(@RequestBody TripRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        request.setAuthorId(userId);
        return tripService.createTrip(request);
    }

    @PutMapping("/{id}")
    public TripResponse updateTrip(@PathVariable Long id, @RequestBody TripRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return tripService.updateTrip(id, request, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteTrip(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        tripService.deleteTrip(id, userId);
    }

    // ----------------- Photo Upload APIs -----------------
    @PostMapping("/{id}/photos")
    public ResponseEntity<?> uploadPhotos(
            @PathVariable Long id,
            @RequestParam("photos") List<MultipartFile> photos) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "กรุณา login ก่อนอัพโหลดรูปภาพ"
            ));
        }

        try {
            TripResponse updatedTrip = tripService.uploadPhotos(id, photos, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "อัพโหลดรูปภาพสำเร็จ",
                    "data", updatedTrip
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "เกิดข้อผิดพลาดในการอัพโหลด: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}/photos")
    public ResponseEntity<?> deletePhoto(
            @PathVariable Long id,
            @RequestParam("photoUrl") String photoUrl) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "กรุณา login ก่อนลบรูปภาพ"
            ));
        }

        try {
            TripResponse updatedTrip = tripService.deletePhoto(id, photoUrl, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "ลบรูปภาพสำเร็จ",
                    "data", updatedTrip
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "เกิดข้อผิดพลาดในการลบรูปภาพ: " + e.getMessage()
            ));
        }
    }
}