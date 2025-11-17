package com.techup.dto;

import lombok.Builder;
import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class TripResponse {
    private Long id;
    private String title;
    private String description;
    private String[] photos;
    private String[] tags;
    private Double latitude;
    private Double longitude;
    private Long authorId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}