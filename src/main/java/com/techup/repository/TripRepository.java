package com.techup.repository;

import com.techup.entity.Trip;  
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;  // เพิ่มบรรทัดนี้

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> { 
    
    // Override save() เพื่อบังคับ @NonNull
    @NonNull
    @Override
    <S extends Trip> S save(@NonNull S entity);
    
    // หาทริปตาม author_id
    List<Trip> findByAuthorId(Long authorId);
    
    // หาทริปที่มี title ตรงกัน (ค้นหาแบบไม่สนใจตัวพิมพ์)
    List<Trip> findByTitleContainingIgnoreCase(String title);
    
    
    // ค้นหาทริปที่มี tag นี้ (PostgreSQL array operator)
    @Query(value = "SELECT * FROM trips WHERE :tag = ANY(tags)", nativeQuery = true)
    List<Trip> findByTag(@Param("tag") String tag);
    
    // ค้นหาจาก title หรือ description
    @Query("SELECT t FROM Trip t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Trip> searchByKeyword(@Param("keyword") String keyword);
    
    // หาทริปใกล้เคียง (ในรัศมีที่กำหนด) - ใช้ Haversine formula
    @Query(value = "SELECT * FROM trips WHERE " +
           "6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * " +
           "cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(latitude))) <= :radius " +
           "AND latitude IS NOT NULL AND longitude IS NOT NULL", 
           nativeQuery = true)
    List<Trip> findNearby(@Param("lat") double latitude, 
                          @Param("lng") double longitude, 
                          @Param("radius") double radiusInKm);
    
    // หาทริปที่สร้างโดย author (ใช้ email)
    @Query("SELECT t FROM Trip t WHERE t.author.email = :email")
    List<Trip> findByAuthorEmail(@Param("email") String email);
    
    // นับจำนวนทริปของ author
    long countByAuthorId(Long authorId);
}