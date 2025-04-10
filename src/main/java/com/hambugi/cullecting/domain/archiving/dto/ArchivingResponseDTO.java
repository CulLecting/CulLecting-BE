package com.hambugi.cullecting.domain.archiving.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ArchivingResponseDTO {
    private String id;
    private String title;
    private String description;
    private String date;
    private String imageURL;
    private String category;
    private String template;
}
