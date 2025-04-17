package com.hambugi.cullecting.domain.curtural.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CulturalEventFromDateDTO {
    private String title;
    private String imageURL;
    private String place;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
