package com.dn.sistema_asistencia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    // 1. Inyectamos la URL dinámica de producción/local desde el application.properties
    @Value("${app.base-url}")
    private String baseUrl;

    // 2. Inyectamos el nombre y token dinámicos para no dejar datos sensibles expuestos en GitHub
    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final QrGeneratorService qrGeneratorService;

    // Constructor único para inyectar dependencias
    public TelegramBotService(QrGeneratorService qrGeneratorService) {
        this.qrGeneratorService = qrGeneratorService;
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
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
                    // 🔥 CORRECCIÓN: Armamos la URL usando el dominio dinámico (Render o Local)
                    String urlConParametro = baseUrl + "/api/asistencia/registrar?telegramUser=" + telegramUser;

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
                    System.err.println("Error al generar o enviar el código QR: " + e.getMessage());
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
    /**
     * Envía una notificación de texto automática al empleado cuando el backend registra su asistencia.
     */
    public void enviarNotificacionAsistencia(String chatUserId, String mensaje) {
        // Hacemos una validación rápida: si no es un número, no intentamos enviarlo para evitar que rompa el backend
        if (chatUserId == null || !chatUserId.matches("\\d+")) {
            System.err.println("⚠️ No se envió la notificación: '" + chatUserId + "' no es un ChatId numérico válido de Telegram.");
            return; // Termina el método de forma segura sin lanzar errores al controlador
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatUserId);
        sendMessage.setText(mensaje);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Error al enviar la notificación de asistencia por Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }
}