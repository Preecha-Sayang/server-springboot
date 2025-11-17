package com.techup.dto;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String displayName;
    private OffsetDateTime createdAt;
}
