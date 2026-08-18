package com.example.feishubot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotProperties {

    @Value("${bot.springai.app-id:}")
    private String springAiAppId;

    @Value("${bot.springai.app-secret:}")
    private String springAiAppSecret;

    @Value("${bot.springai.backend-url:http://localhost:8080}")
    private String springAiBackendUrl;

    @Value("${bot.agentscope.app-id:}")
    private String agentscopeAppId;

    @Value("${bot.agentscope.app-secret:}")
    private String agentscopeAppSecret;

    @Value("${bot.agentscope.backend-url:http://localhost:8081}")
    private String agentscopeBackendUrl;

    public String getSpringAiAppId() {
        return springAiAppId;
    }

    public String getSpringAiAppSecret() {
        return springAiAppSecret;
    }

    public String getSpringAiBackendUrl() {
        return springAiBackendUrl;
    }

    public String getAgentscopeAppId() {
        return agentscopeAppId;
    }

    public String getAgentscopeAppSecret() {
        return agentscopeAppSecret;
    }

    public String getAgentscopeBackendUrl() {
        return agentscopeBackendUrl;
    }
}
