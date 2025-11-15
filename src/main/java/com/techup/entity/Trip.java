package com.techup.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity 
@Table(name = "trips") 
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Trip {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "photos", columnDefinition = "TEXT[]")
    private String[] photos;
    
    @Column(name = "tags", columnDefinition = "TEXT[]")
    private String[] tags;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // กำหนดค่า default สำหรับ arrays
        if (photos == null) {
            photos = new String[]{};
        }
        if (tags == null) {
            tags = new String[]{};
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


// -- ตารางทริป (เพิ่ม location)
// CREATE TABLE trips (
//   id BIGSERIAL PRIMARY KEY,
//   title TEXT NOT NULL,
//   description TEXT,
//   photos TEXT[] NOT NULL DEFAULT '{}',
//   tags TEXT[] NOT NULL DEFAULT '{}',
//   latitude DOUBLE PRECISION,
//   longitude DOUBLE PRECISION,
//   author_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
//   created_at TIMESTAMPTZ DEFAULT NOW(),
//   updated_at TIMESTAMPTZ DEFAULT NOW()
// );