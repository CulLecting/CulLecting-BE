package com.hambugi.cullecting.domain.curtural.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LatestCulturalEventDTO {
    private Map<String, List<CulturalEventFromDateResponseDTO>> result;
}
