package com.techup.controller;

import com.techup.dto.TripRequest;
import com.techup.dto.TripResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.techup.service.TripService;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

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
    public TripResponse createTrip(@RequestBody TripRequest request) {
        return tripService.createTrip(request);
    }

    // แก้ไขทริปตัวเอง
    @PutMapping("/{id}")
    public TripResponse updateTrip(
            @PathVariable Long id,
            @RequestBody TripRequest request,
            @RequestParam Long authorId) { // เพิ่ม authorId
        return tripService.updateTrip(id, request, authorId);
    }

    // ลบทริปตัวเอง
    @DeleteMapping("/{id}")
    public void deleteTrip(@PathVariable Long id, @RequestParam Long authorId) {
        tripService.deleteTrip(id, authorId);
    }
}