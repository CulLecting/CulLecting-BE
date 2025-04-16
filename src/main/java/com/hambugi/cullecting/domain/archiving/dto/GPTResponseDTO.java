package com.hambugi.cullecting.domain.archiving.dto;

import lombok.Data;

import java.util.List;

@Data
public class GPTResponseDTO {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
