package com.example.demo.config;

import com.example.demo.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {
    
    private final TelegramBotService telegramBotService;
    
    @Bean
    public TelegramBotsApi telegramBotsApi() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
            log.info("Telegram Bot успешно зарегистрирован и запущен");
            return botsApi;
        } catch (TelegramApiException e) {
            log.error("Ошибка при регистрации Telegram Bot: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось зарегистрировать Telegram Bot", e);
        }
    }
}