package com.example.feishubot;

import java.net.URI;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class AgentClient {

    private static final Logger log = LoggerFactory.getLogger(AgentClient.class);

    private final WebClient webClient = WebClient.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(String baseUrl, String chatId, String message) throws Exception {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/chat")
                .queryParam("conversationId", chatId)
                .queryParam("message", message)
                .build()
                .encode()
                .toUri();
        String response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .block();
        JsonNode node = objectMapper.readTree(response);
        String reply = node.path("reply").asText(null);
        if (reply == null) {
            log.warn("后端 {} 返回异常: {}", baseUrl, response);
            return node.path("error").asText("后端返回异常，请稍后再试。");
        }
        return reply;
    }
}
