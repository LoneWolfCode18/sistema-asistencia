package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.Asistencia;
import com.dn.sistema_asistencia.entity.Usuario;
import com.dn.sistema_asistencia.repository.AsistenciaRepository;
import com.dn.sistema_asistencia.repository.UsuarioRepository;
import com.dn.sistema_asistencia.service.TelegramBotService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController // 🔥 Cambiado a RestController para devolver HTML/Texto directo sin depender de archivos .html
public class AsistenciaController {

    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final TelegramBotService telegramBotService;

    // Inyección de dependencias por constructor
    public AsistenciaController(UsuarioRepository usuarioRepository,
                                AsistenciaRepository asistenciaRepository,
                                TelegramBotService telegramBotService) {
        this.usuarioRepository = usuarioRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.telegramBotService = telegramBotService;
    }

    // 🔥 Agregamos 'produces' para que el navegador renderice el texto como una página web bonita
    @GetMapping(value = "/api/asistencia/registrar", produces = MediaType.TEXT_HTML_VALUE)
    public String registrarAsistencia(@RequestParam String telegramUser) {

        // 1. Buscamos al usuario en la BD por su cuenta de Telegram
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTelegramUser(telegramUser);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // 2. Registramos la asistencia usando el patrón Builder
            LocalDate hoy = LocalDate.now();
            LocalTime horaActual = LocalTime.now();

            // Regla de negocio para la tolerancia: La hora límite es las 09:15 AM
            LocalTime horaLimite = LocalTime.of(9, 15);
            String estadoAsistencia = !horaActual.isAfter(horaLimite) ? "A TIEMPO" : "TARDANZA";

            Asistencia nuevaAsistencia = Asistencia.builder()
                    .usuario(usuario)
                    .fecha(hoy)
                    .horaEntrada(horaActual)
                    .estadoAsistencia(estadoAsistencia)
                    .build();

            // 3. Guardamos la asistencia en la base de datos
            asistenciaRepository.save(nuevaAsistencia);

            // Formateamos la hora
            String horaFormateada = horaActual.format(DateTimeFormatter.ofPattern("hh:mm a"));

            // 4. Intento seguro de notificación por Telegram
            try {
                String mensajeAlert = "🔔 ¡Hola " + usuario.getNombre() + "! Tu ingreso (" + estadoAsistencia + ") ha sido registrado con éxito a las " + horaFormateada + ".";

                // NOTA: Si da error por el formato del username, el try-catch evita que colapse el registro de la BD
                telegramBotService.enviarNotificacionAsistencia(usuario.getTelegramUser(), mensajeAlert);
            } catch (Exception e) {
                System.err.println("Aviso: No se pudo enviar el mensaje de Telegram (Requiere ChatId numérico): " + e.getMessage());
            }

            // 5. Retornamos una interfaz HTML limpia renderizada directamente en el celular del usuario
            return "<div style='text-align: center; font-family: sans-serif; padding: 50px;'>" +
                    "<h1 style='color: #2e7d32;'>✔️ ¡Asistencia Registrada!</h1>" +
                    "<p style='font-size: 18px;'>👋 Hola, <strong>" + usuario.getNombre() + "</strong>.</p>" +
                    "<p style='font-size: 16px;'>Tu ingreso ha sido marcado como: <span style='font-weight:bold; color:" +
                    (estadoAsistencia.equals("A TIEMPO") ? "#2e7d32" : "#c62828") + ";'>" + estadoAsistencia + "</span></p>" +
                    "<p style='color: #555;'>🕒 Hora de registro: " + horaFormateada + "</p>" +
                    "<br><p style='font-size: 12px; color:#aaa;'>Agencia DN - Sistema de Asistencia</p>" +
                    "</div>";
        } else {
            // Retorno si el usuario no es encontrado (Como la prueba ShadyTest)
            return "<div style='text-align: center; font-family: sans-serif; padding: 50px;'>" +
                    "<h1 style='color: #d32f2f;'>❌ Usuario No Encontrado</h1>" +
                    "<p style='font-size: 18px;'>El usuario de Telegram <strong>@" + telegramUser + "</strong> no está registrado en la base de datos.</p>" +
                    "<p style='color: #777;'>Por favor, asegúrate de registrar primero al empleado antes de escanear el código QR.</p>" +
                    "<br><p style='font-size: 12px; color:#aaa;'>Agencia DN - Sistema de Asistencia</p>" +
                    "</div>";
        }
    }
}