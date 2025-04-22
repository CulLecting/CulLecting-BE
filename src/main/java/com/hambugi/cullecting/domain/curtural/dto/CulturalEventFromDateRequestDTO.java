package com.hambugi.cullecting.domain.curtural.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CulturalEventFromDateRequestDTO {
    private LocalDate date;
}
