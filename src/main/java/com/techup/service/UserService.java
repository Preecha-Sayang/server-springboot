package com.techup.service;

import com.techup.dto.UserRequest;
import com.techup.dto.UserResponse;
import com.techup.dto.UserLoginRequest;
import com.techup.entity.User;
import com.techup.repository.UserRepository;
import com.techup.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;  // เพิ่มนี้

    // Register
    public UserResponse register(UserRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setDisplayName(request.getDisplayName());
        user.setCreatedAt(OffsetDateTime.now());

        userRepository.save(user);
        
        // สร้าง token เมื่อ register
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return toResponse(user, token);
    }

    // Login
    public UserResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if(!BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        // สร้าง token เมื่อ login
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return toResponse(user, token);
    }

    // Mapping entity → DTO (with token)
    private UserResponse toResponse(User user, String token) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .token(token)  // เพิ่ม token ในตรงนี้
                .build();
    }
}
