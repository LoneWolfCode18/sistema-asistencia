package com.dn.sistema_asistencia.service;

import com.dn.sistema_asistencia.entity.BitacoraMuro;
import com.dn.sistema_asistencia.entity.Usuario;
import com.dn.sistema_asistencia.repository.BitacoraMuroRepository;
import com.dn.sistema_asistencia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final QrGeneratorService qrGeneratorService;

    // 🌟 INYECTAMOS LOS REPOSITORIOS PARA ALIMENTAR SUPABASE
    private final UsuarioRepository usuarioRepository;
    private final BitacoraMuroRepository bitacoraMuroRepository;

    // Constructor único actualizando la inyección de dependencias
    public TelegramBotService(QrGeneratorService qrGeneratorService,
                              UsuarioRepository usuarioRepository,
                              BitacoraMuroRepository bitacoraMuroRepository) {
        this.qrGeneratorService = qrGeneratorService;
        this.usuarioRepository = usuarioRepository;
        this.bitacoraMuroRepository = bitacoraMuroRepository;
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            String telegramUser = update.getMessage().getFrom().getUserName();
            if (telegramUser == null || telegramUser.isEmpty()) {
                telegramUser = update.getMessage().getFrom().getFirstName();
            }

            // ==========================================
            // 🎫 FLUJO DE ASISTENCIA / QR (Tu código base)
            // ==========================================
            if (messageText.equalsIgnoreCase("/asistencia") || messageText.equalsIgnoreCase("/qr")) {
                try {
                    String urlConParametro = baseUrl + "/api/asistencia/registrar?telegramUser=" + telegramUser;
                    byte[] qrBytes = qrGeneratorService.generateQrCodeImage(urlConParametro, 300, 300);

                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(String.valueOf(chatId));
                    sendPhoto.setPhoto(new InputFile(new ByteArrayInputStream(qrBytes), "qr_" + telegramUser + ".png"));
                    sendPhoto.setCaption("👋 ¡Hola " + telegramUser + "! Aquí tienes tu código QR dinámico para registrar tu asistencia en la Agencia DN. Escanéalo en la entrada.");

                    execute(sendPhoto);
                } catch (Exception e) {
                    System.err.println("Error al generar o enviar el código QR: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // ==========================================
            // 📝 NUEVO FLUJO: COMANDO /muro PARA REPORTE DIARIO
            // ==========================================
            else if (messageText.startsWith("/muro ")) {
                try {
                    // Extraemos el texto del reporte quitando el "/muro "
                    String reporteLimpio = messageText.replace("/muro ", "").trim();

                    if (reporteLimpio.isEmpty()) {
                        enviarMensajeTexto(chatId, "⚠️ El reporte no puede estar vacío. Escribe algo después de /muro");
                        return;
                    }

                    // Buscamos al usuario en la BD de Supabase usando su username de Telegram
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByTelegramUser(telegramUser);
                    if (usuarioOpt.isPresent()) {
                        Usuario usuario = usuarioOpt.get();

                        // Construimos la bitácora que alimentará el Muro Web
                        BitacoraMuro nuevaBitacora = BitacoraMuro.builder()
                                .usuario(usuario)
                                .fecha(LocalDate.now())
                                .horaRegistro(LocalTime.now())
                                .aprendizaje(reporteLimpio)
                                .utilidadConteo(0) // Empieza con 0 reacciones
                                .build();

                        // Guardamos de forma nativa en la base de datos
                        bitacoraMuroRepository.save(nuevaBitacora);

                        // Confirmamos el éxito directo al celular del programador
                        enviarMensajeTexto(chatId, "✅ ¡Excelente! Tu bitácora ha sido publicada exitosamente en el Muro de la Agencia DN.");
                    } else {
                        enviarMensajeTexto(chatId, "❌ No estás registrado en el sistema web de asistencia. Avisa a tu coordinador.");
                    }

                } catch (Exception e) {
                    System.err.println("Error al procesar el comando /muro: " + e.getMessage());
                    enviarMensajeTexto(chatId, "❌ Ocurrió un error interno en el servidor al intentar publicar tu muro.");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Método auxiliar para enviar respuestas rápidas de texto
     */
    private void enviarMensajeTexto(long chatId, String texto) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(texto);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Error al enviar mensaje de texto: " + e.getMessage());
        }
    }

    /**
     * Envía una notificación de texto automática al empleado cuando el backend registra su asistencia.
     */
    public void enviarNotificacionAsistencia(String chatUserId, String mensaje) {
        if (chatUserId == null || !chatUserId.matches("\\d+")) {
            System.err.println("⚠️ No se envió la notificación: '" + chatUserId + "' no es un ChatId numérico válido de Telegram.");
            return;
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