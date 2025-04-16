package com.hambugi.cullecting.domain.archiving.service;

import com.hambugi.cullecting.domain.archiving.dto.GPTResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GPTService {
    private final WebClient.Builder webClientBuilder;

    public GPTService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    @Value("${openai.gpt.key}")
    private String apiKey;

    public List<String> analyzeCodenameList(Map<String, String> data) {
        String prompt = buildCodenamePrompt(data);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        WebClient webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();

        GPTResponseDTO response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            System.out.println("💥 429 에러: 재시도 대기");
                            return Mono.error(new RuntimeException("Too many requests"));
                        })
                )
                .bodyToMono(GPTResponseDTO.class)
                .block();

        List<String> result = convertStringToList(response.getChoices().get(0).getMessage().getContent());
        return result;
    }
    // 프롬프트 수정해야함
    private String buildCodenamePrompt(Map<String, String> data) {
        String result = formatTitleCodenameMap(data);
        System.out.println(result);
        return String.format("""
            
                아래는 사용자의 문화적 관심 카테고리 목록입니다.
                일부 항목은 **중복해서 등장할 수 있으며**, 이는 해당 분야에 더 높은 관심이 있다는 뜻입니다:
        
                %s
            
                각 데이터들은 하나의 문화 분야를 대표합니다.
                단, 각각의 데이터는 다음과 같은 세부 카테고리를 포함합니다 (GPT가 유추해서 반영해주세요):
            
                예시:
                - 공연/예술 → 뮤지컬, 오페라, 무용, 연극, 영화 등
                - 음악 → 클래식, 국악, 콘서트, 독주회 등
                - 전시/미술 → 미술 전시, 사진전, 아트페어 등
                - 축제/야외체험 → 시민 참여형 축제, 전통 문화 축제, 자연 체험 활동 등
                - 교육/체험 → 어린이 체험학습, 문화교실, 역사 체험 등
                - 문화/예술 일반 → 전통 및 현대 문화행사 전반
                - 기타 → 기타 특별한 활동들
        
                이 데이터를 참고하여, 사용자가 선호할 만한 **문화 키워드**를 4~6개로 요약해주세요.
        
                - 중복된 항목은 **더 높은 비중으로 반영**해주세요.
                - 각 키워드는 **짧고 감성적인 문구 + 관련 이모지** 형태로 작성해주세요.
                - 전체적인 톤은 **부드럽고 직관적이며**, 실제 취향 분석처럼 자연스럽게 만들어주세요.
                - 불필요한 설명 없이, 키워드만 리스트 형식으로 정리해주세요.
        
                예시 출력 형식:
                - 🎨 창의적인 감성 \s
                - 🎶 여운이 남는 멜로디 \s
                - 🎭 무대 위 이야기 \s
            """, result);
    }

    private String formatTitleCodenameMap(Map<String, String> map) {
        return map.entrySet().stream()
                .map(entry -> "- " + entry.getKey() + " : " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    private List<String> convertStringToList(String input) {
        return Arrays.stream(input.split("\n"))
                .map(line -> line.replaceFirst("-\\s*", "").trim())
                .collect(Collectors.toList());
    }

}
