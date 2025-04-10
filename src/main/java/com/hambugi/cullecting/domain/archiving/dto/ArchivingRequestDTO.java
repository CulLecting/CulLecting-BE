package com.hambugi.cullecting.domain.archiving.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArchivingRequestDTO {
    private String title;
    private String description;
    private String date;
    private String category;
    private String template;
}
