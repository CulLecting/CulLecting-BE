package com.hambugi.cullecting.domain.curtural.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CulturalEventResponseDTO {
    private long id;
    private String title;
    private String imageURL;
    private String place;
    private LocalDate startDate;
    private LocalDate endDate;
}
