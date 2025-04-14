package com.hambugi.cullecting.domain.archiving.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArchivingUpdateRequestDTO {
    private String id;
    private String title;
    private String description;
    private String date;
    private String category;
}
