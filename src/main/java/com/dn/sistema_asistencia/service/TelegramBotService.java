package com.dn.sistema_asistencia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage; // <-- Importado para las alertas de texto
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private final QrGeneratorService qrGeneratorService;

    // Inyectamos tu servicio de QR genérico
    public TelegramBotService(QrGeneratorService qrGeneratorService) {
        this.qrGeneratorService = qrGeneratorService;
    }

    @Override
    public String getBotUsername() {
        // Aquí pones el nombre de tu bot de Telegram
        return "dn_gestor_asistencia_bot";
    }

    @Override
    public String getBotToken() {
        // Token entregado por BotFather
        return "8156981786:AAFM4vLJH_J3bbggt9KtN4V1fKzVPrqcwHM";
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Validamos que el mensaje tenga texto
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Capturamos el usuario de Telegram de la persona que escribe
            String telegramUser = update.getMessage().getFrom().getUserName();

            // Si el usuario no tiene un @username configurado en Telegram, usamos su nombre
            if (telegramUser == null || telegramUser.isEmpty()) {
                telegramUser = update.getMessage().getFrom().getFirstName();
            }

            // Si escribe /asistencia o /qr
            if (messageText.equalsIgnoreCase("/asistencia") || messageText.equalsIgnoreCase("/qr")) {
                try {
                    // 1. Armamos la URL dinámica con tu IP de la laptop
                    String urlConParametro = "http://10.53.111.211:8080/api/asistencia/registrar?telegramUser=" + telegramUser;

                    // 2. Llamamos a tu servicio para que genere los bytes de la imagen QR
                    byte[] qrBytes = qrGeneratorService.generateQrCodeImage(urlConParametro, 300, 300);

                    // 3. Preparamos el mensaje con la foto del QR para Telegram
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(String.valueOf(chatId));
                    sendPhoto.setPhoto(new InputFile(new ByteArrayInputStream(qrBytes), "qr_" + telegramUser + ".png"));
                    sendPhoto.setCaption("👋 ¡Hola " + telegramUser + "! Aquí tienes tu código QR dinámico para registrar tu asistencia en la Agencia DN. Escanéalo en la entrada.");

                    // 4. Enviamos la foto al chat
                    execute(sendPhoto);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Envía una notificación de texto automática al empleado cuando el backend registra su asistencia.
     * @param chatUserId El identificador de Telegram (puede ser el username o chatId si lo manejas numérico)
     * @param mensaje El contenido de la alerta de asistencia
     */
    public void enviarNotificacionAsistencia(String chatUserId, String mensaje) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatUserId);
        sendMessage.setText(mensaje);

        try {
            execute(sendMessage); // Envía el mensaje de texto directo al celular del usuario
        } catch (TelegramApiException e) {
            System.err.println("Error al enviar la notificación de asistencia por Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }
}