package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.Asistencia;
import com.dn.sistema_asistencia.entity.Usuario;
import com.dn.sistema_asistencia.repository.AsistenciaRepository;
import com.dn.sistema_asistencia.repository.UsuarioRepository;
import com.dn.sistema_asistencia.service.TelegramBotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
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

    @GetMapping("/api/asistencia/registrar")
    public String registrarAsistencia(@RequestParam String telegramUser, Model model) {

        // 1. Buscamos al usuario en la BD por su cuenta de Telegram
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTelegramUser(telegramUser);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // 2. Registramos la asistencia usando el patrón Builder de la entidad Asistencia
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

            // Formateamos la hora para la notificación y la vista
            String horaFormateada = horaActual.format(DateTimeFormatter.ofPattern("hh:mm a"));

            // 4. Invocamos a telegramBotService para notificar al empleado usando mensajeAlert
            String mensajeAlert = "🔔 ¡Hola " + usuario.getNombre() + "! Tu ingreso (" + estadoAsistencia + ") ha sido registrado con éxito a las " + horaFormateada + ".";
            telegramBotService.enviarNotificacionAsistencia(usuario.getTelegramUser(), mensajeAlert);

            // 5. Retornos de vistas: agregamos al modelo el nombreUsuario y la hora, y retornamos la vista de éxito
            model.addAttribute("nombreUsuario", usuario.getNombre());
            model.addAttribute("hora", horaFormateada);
            return "asistencia-exito";
        } else {
            // Retorno si el usuario no es encontrado
            model.addAttribute("telegramUser", telegramUser);
            return "asistencia-error";
        }
    }
}