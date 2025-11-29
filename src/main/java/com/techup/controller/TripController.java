package com.techup.controller;

import com.techup.dto.TripRequest;
import com.techup.dto.TripResponse;
import com.techup.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.techup.service.TripService;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final JwtService jwtService;

    // ----------------- Visitor APIs -----------------

    // ดูทริปทั้งหมด
    @GetMapping
    public List<TripResponse> getAllTrips() {
        return tripService.getAllTrips();
    }

    // Search ทริป (ตาม title หรือ province/tag)
    @GetMapping("/search")
    public List<TripResponse> searchTrips(@RequestParam String keyword) {
        return tripService.searchByKeyword(keyword);
    }

    // ดูรายละเอียดทริป
    @GetMapping("/{id}")
    public TripResponse getTripDetail(@PathVariable Long id) {
        return tripService.getTripById(id);
    }

    // ----------------- Authenticated User APIs -----------------

    // สร้างทริปใหม่ (ต้อง login)
    @PostMapping
    public TripResponse createTrip(@RequestBody TripRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        request.setAuthorId(userId);
        return tripService.createTrip(request);
    }

    // แก้ไขทริปตัวเอง (ดึง userId จาก token โดยอัตโนมัติ)
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

    // ลบทริปตัวเอง (ดึง userId จาก token โดยอัตโนมัติ)
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

    // Helper method: ดึง userId จาก JWT token
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