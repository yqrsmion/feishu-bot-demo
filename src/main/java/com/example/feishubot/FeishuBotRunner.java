package com.example.feishubot;

import com.lark.oapi.channel.ChannelEventHandler;
import com.lark.oapi.channel.LarkChannel;
import com.lark.oapi.channel.LarkChannelFactory;
import com.lark.oapi.channel.config.LarkChannelOptions;
import com.lark.oapi.channel.model.NormalizedMessage;
import com.lark.oapi.channel.model.SendInput;
import com.lark.oapi.channel.model.SendOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class FeishuBotRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FeishuBotRunner.class);

    private final BotProperties props;
    private final AgentClient agentClient;

    public FeishuBotRunner(BotProperties props, AgentClient agentClient) {
        this.props = props;
        this.agentClient = agentClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        startBot("SpringAI", props.getSpringAiAppId(), props.getSpringAiAppSecret(),
                props.getSpringAiBackendUrl());
        startBot("AgentScope", props.getAgentscopeAppId(), props.getAgentscopeAppSecret(),
                props.getAgentscopeBackendUrl());
        Thread.currentThread().join();
    }

    private void startBot(String name, String appId, String appSecret, String backendUrl) throws Exception {
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            log.warn("[{}] 凭证未配置，跳过（请设置环境变量 FEISHU_{}_APP_ID / FEISHU_{}_APP_SECRET）",
                    name, name.toUpperCase(), name.toUpperCase());
            return;
        }
        LarkChannel channel = LarkChannelFactory.createLarkChannel(
                LarkChannelOptions.newBuilder(appId, appSecret)
                        .transport("websocket")
                        .build());

        channel.on("message", new ChannelEventHandler<NormalizedMessage>() {
            @Override
            public void handle(NormalizedMessage message) {
                try {
                    String content = message.getContent() == null
                            ? ""
                            : message.getContent().replaceAll("@\\S+", "").trim();
                    if (content.isEmpty()) {
                        return;
                    }
                    log.info("[{}] 收到群消息 chatId={} 内容={}", name, message.getChatId(), content);
                    String answer = agentClient.chat(backendUrl, message.getChatId(), content);
                    channel.send(
                            message.getChatId(),
                            SendInput.markdown(answer),
                            SendOptions.newBuilder().replyTo(message.getMessageId()).build());
                } catch (Exception e) {
                    log.error("[{}] 处理消息失败", name, e);
                    try {
                        channel.send(
                                message.getChatId(),
                                SendInput.text("处理失败，请稍后再试。"),
                                SendOptions.newBuilder().replyTo(message.getMessageId()).build());
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
            }
        });

        var identity = channel.connect().get();
        log.info("[{}] 机器人已连接，openId={}，后端={}", name, identity.getOpenId(), backendUrl);
    }
}
