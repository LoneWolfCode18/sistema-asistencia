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

@RestController // 🎯 Devuelve HTML directo al navegador del celular
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

    @GetMapping(value = "/api/asistencia/registrar", produces = MediaType.TEXT_HTML_VALUE)
    public String registrarAsistencia(@RequestParam String telegramUser) {

        // 1. Buscamos al usuario en la BD por su cuenta de Telegram
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTelegramUser(telegramUser);

        if (usuarioOpt.isEmpty()) {
            // Retorno si el usuario no es encontrado (Como la prueba ShadyTest)
            return "<div style='text-align: center; font-family: sans-serif; padding: 50px;'>" +
                    "<h1 style='color: #d32f2f;'>❌ Usuario No Encontrado</h1>" +
                    "<p style='font-size: 18px;'>El usuario de Telegram <strong>@" + telegramUser + "</strong> no está registrado en la base de datos.</p>" +
                    "<p style='color: #777;'>Por favor, asegúrate de registrar primero al empleado antes de escanear el código QR.</p>" +
                    "<br><p style='font-size: 12px; color:#aaa;'>Agencia DN - Sistema de Asistencia</p>" +
                    "</div>";
        }

        Usuario usuario = usuarioOpt.get();
        LocalDate hoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        // Regla de negocio para la tolerancia: La hora límite es las 09:15 AM
        LocalTime horaLimite = LocalTime.of(9, 15);
        String estadoAsistencia = !horaActual.isAfter(horaLimite) ? "A TIEMPO" : "TARDANZA";

        // 2. Construimos el objeto de asistencia con tu patrón Builder perfecto
        Asistencia nuevaAsistencia = Asistencia.builder()
                .usuario(usuario)
                .fecha(hoy)
                .horaEntrada(horaActual)
                .estadoAsistencia(estadoAsistencia)
                .build();

        // 3. Intento seguro de guardado en la base de datos
        try {
            asistenciaRepository.save(nuevaAsistencia);
        } catch (Exception e) {
            // 🔥 CAPTURA: Evita el Error 500 si viola el UniqueConstraint de Supabase (usuario_id + fecha)
            return "<div style='text-align: center; font-family: sans-serif; padding: 50px;'>" +
                    "<h1 style='color: #f57c00;'>⚠️ Ya Registrado</h1>" +
                    "<p style='font-size: 18px;'>👋 Hola, <strong>" + usuario.getNombre() + "</strong>.</p>" +
                    "<p style='font-size: 16px; color: #555;'>Tu asistencia para el día de hoy (<strong>" + hoy + "</strong>) ya fue marcada previamente.</p>" +
                    "<p style='color: #777;'>No es necesario volver a escanear el código QR.</p>" +
                    "<br><p style='font-size: 12px; color:#aaa;'>Agencia DN - Sistema de Asistencia</p>" +
                    "</div>";
        }

        // Formateamos la hora para la vista de éxito y la notificación
        String horaFormateada = horaActual.format(DateTimeFormatter.ofPattern("hh:mm a"));

        // 4. Intento seguro de notificación por Telegram
        try {
            String mensajeAlert = "🔔 ¡Hola " + usuario.getNombre() + "! Tu ingreso (" + estadoAsistencia + ") ha sido registrado con éxito a las " + horaFormateada + ".";
            telegramBotService.enviarNotificacionAsistencia(usuario.getTelegramUser(), mensajeAlert);
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo enviar el mensaje de Telegram: " + e.getMessage());
        }

        // 5. Retornamos la interfaz HTML verde de éxito rotundo
        return "<div style='text-align: center; font-family: sans-serif; padding: 50px;'>" +
                "<h1 style='color: #2e7d32;'>✔️ ¡Asistencia Registrada!</h1>" +
                "<p style='font-size: 18px;'>👋 Hola, <strong>" + usuario.getNombre() + "</strong>.</p>" +
                "<p style='font-size: 16px;'>Tu ingreso ha sido marcado como: <span style='font-weight:bold; color:" +
                (estadoAsistencia.equals("A TIEMPO") ? "#2e7d32" : "#c62828") + ";'>" + estadoAsistencia + "</span></p>" +
                "<p style='color: #555;'>🕒 Hora de registro: " + horaFormateada + "</p>" +
                "<br><p style='font-size: 12px; color:#aaa;'>Agencia DN - Sistema de Asistencia</p>" +
                "</div>";
    }
}