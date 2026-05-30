package com.dn.sistema_asistencia.config;

import com.dn.sistema_asistencia.service.TelegramBotService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(telegramBotService);
            System.out.println("=================================================");
            System.out.println("🤖 ¡BOT DE TELEGRAM CONECTADO Y ENCHUFADO! 🚀");
            System.out.println("=================================================");
        } catch (TelegramApiException e) {
            System.err.println("❌ Error al conectar el bot: " + e.getMessage());
        }
        return botsApi;
    }
}