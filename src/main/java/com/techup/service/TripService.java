package com.techup.service;

import com.techup.repository.UserRepository;
import com.techup.repository.TripRepository;
import com.techup.entity.Trip;
import com.techup.dto.TripRequest;
import com.techup.dto.TripResponse;
import com.techup.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    // ดูรายการทริปทั้งหมด
    public List<TripResponse> getAllTrips() {
        List<Trip> trips = tripRepository.findAll();
        List<TripResponse> responseList = new ArrayList<>();
        for (Trip trip : trips) {
            responseList.add(toResponse(trip));
        }
        return responseList;
    }

    // ดูรายละเอียดทริปตาม id
    public TripResponse getTripById(@NonNull Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        return toResponse(trip);
    }

    // ค้นหาทริปตาม keyword
    public List<TripResponse> searchByKeyword(@NonNull String keyword) {
        List<Trip> trips = tripRepository.searchByKeyword(keyword);
        List<TripResponse> responseList = new ArrayList<>();
        for (Trip trip : trips) {
            responseList.add(toResponse(trip));
        }
        return responseList;
    }

    // สร้างทริปใหม่
    @SuppressWarnings("null")
    public TripResponse createTrip(@NonNull TripRequest request) {
        Long authorId = request.getAuthorId();
        if (authorId == null) throw new RuntimeException("AuthorId is null");

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Trip trip = Trip.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .photos(request.getPhotos() != null ? request.getPhotos().toArray(new String[0]) : new String[]{})
                .tags(request.getTags() != null ? request.getTags().toArray(new String[0]) : new String[]{})
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .author(author)
                .build();

        Trip savedTrip = tripRepository.save(trip);
        return toResponse(savedTrip);
    }

    // แก้ไขทริป (ต้องเป็นเจ้าของ)
    public TripResponse updateTrip(@NonNull Long tripId, @NonNull TripRequest request, @NonNull Long userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // ✅ ตรวจสอบสิทธิ์: ต้องเป็นเจ้าของทริป
        if (trip.getAuthor() == null || !trip.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to edit this trip");
        }

        trip.setTitle(request.getTitle());
        trip.setDescription(request.getDescription());
        trip.setPhotos(request.getPhotos() != null ? request.getPhotos().toArray(new String[0]) : new String[]{});
        trip.setTags(request.getTags() != null ? request.getTags().toArray(new String[0]) : new String[]{});
        trip.setLatitude(request.getLatitude());
        trip.setLongitude(request.getLongitude());

        Trip savedTrip = tripRepository.save(trip);
        return toResponse(savedTrip);
    }

    // ลบทริป (ต้องเป็นเจ้าของ)
    public void deleteTrip(@NonNull Long tripId, @NonNull Long userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // ✅ ตรวจสอบสิทธิ์: ต้องเป็นเจ้าของทริป
        if (trip.getAuthor() == null || !trip.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this trip");
        }

        tripRepository.delete(trip);
    }

    // Helper Method
    private TripResponse toResponse(Trip trip) {
        Long authorId = trip.getAuthor() != null ? trip.getAuthor().getId() : null;

        return TripResponse.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .description(trip.getDescription())
                .photos(trip.getPhotos())
                .tags(trip.getTags())
                .latitude(trip.getLatitude())
                .longitude(trip.getLongitude())
                .authorId(authorId)
                .createdAt(trip.getCreatedAt() != null ? trip.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(trip.getUpdatedAt() != null ? trip.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }
}
