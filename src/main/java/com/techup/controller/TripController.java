package com.techup.controller;

import com.techup.dto.TripRequest;
import com.techup.dto.TripResponse;
import com.techup.security.JwtService;
import com.techup.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final JwtService jwtService;

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

    @PostMapping
    public TripResponse createTrip(
            @RequestBody TripRequest request, 
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        request.setAuthorId(userId);
        return tripService.createTrip(request);
    }

    @PutMapping("/{id}")
    public TripResponse updateTrip(
            @PathVariable Long id,
            @RequestBody TripRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return tripService.updateTrip(id, request, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteTrip(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        tripService.deleteTrip(id, userId);
    }

    // ----------------- Photo Upload APIs -----------------

    // อัพโหลดรูปภาพเข้าทริป
    @PostMapping("/{id}/photos")
    public ResponseEntity<?> uploadPhotos(
            @PathVariable Long id,
            @RequestParam("photos") List<MultipartFile> photos,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "กรุณา login ก่อนอัพโหลดรูปภาพ"
                ));
            }

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

    // ลบรูปภาพออกจากทริป
    @DeleteMapping("/{id}/photos")
    public ResponseEntity<?> deletePhoto(
            @PathVariable Long id,
            @RequestParam("photoUrl") String photoUrl,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "กรุณา login ก่อนลบรูปภาพ"
                ));
            }

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

    // Helper method
    private Long extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            return jwtService.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}