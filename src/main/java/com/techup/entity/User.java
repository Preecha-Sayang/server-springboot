package com.techup.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity 
@Table(name = "users") 
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 

public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 255)
    private String email;
    
    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;
    
    @Column(name = "display_name", length = 100)
    private String displayName;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}


// -- ตารางผู้ใช้
// CREATE TABLE users (
//   id BIGSERIAL PRIMARY KEY,
//   email VARCHAR(255) UNIQUE NOT NULL,
//   password_hash TEXT NOT NULL,
//   display_name VARCHAR(100),
//   created_at TIMESTAMPTZ DEFAULT NOW()
// );