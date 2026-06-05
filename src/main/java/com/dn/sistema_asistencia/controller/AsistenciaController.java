package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.Asistencia;
import com.dn.sistema_asistencia.entity.Usuario;
import com.dn.sistema_asistencia.repository.AsistenciaRepository;
import com.dn.sistema_asistencia.repository.UsuarioRepository;
import com.dn.sistema_asistencia.service.TelegramBotService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@CrossOrigin(origins = "*") // 🔥 Agregado para permitir peticiones desde tu dashboard
public class AsistenciaController {

    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final TelegramBotService telegramBotService;

    public AsistenciaController(UsuarioRepository usuarioRepository,
                                AsistenciaRepository asistenciaRepository,
                                TelegramBotService telegramBotService) {
        this.usuarioRepository = usuarioRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.telegramBotService = telegramBotService;
    }

    @GetMapping(value = "/api/asistencia/registrar", produces = MediaType.TEXT_HTML_VALUE)
    public String registrarAsistencia(@RequestParam String telegramUser) {
        Usuario usuario = usuarioRepository.findByTelegramUser(telegramUser).orElse(null);
        if (usuario == null) return "<h1>❌ Usuario No Encontrado</h1>";

        LocalDate hoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();
        String estadoAsistencia = !horaActual.isAfter(LocalTime.of(9, 15)) ? "A TIEMPO" : "TARDANZA";

        Asistencia nuevaAsistencia = Asistencia.builder()
                .usuario(usuario)
                .fecha(hoy)
                .horaEntrada(horaActual)
                .estadoAsistencia(estadoAsistencia)
                .build();

        try {
            asistenciaRepository.save(nuevaAsistencia);
        } catch (Exception e) {
            return "<h1>⚠️ Ya Registrado</h1>";
        }

        return "<h1>✔️ ¡Asistencia Registrada!</h1>";
    }

    // 🔥 Permite al coordinador justificar tardanzas
    @PutMapping("/api/asistencia/justificar/{id}")
    public ResponseEntity<Asistencia> justificar(@PathVariable Long id) {
        return asistenciaRepository.findById(id)
                .map(a -> {
                    a.setEstadoAsistencia("JUSTIFICADO");
                    return ResponseEntity.ok(asistenciaRepository.save(a));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 🚀 NUEVO: Este método soluciona el Error 404 al listar todas las asistencias en el dashboard
    @GetMapping("/api/asistencia/todas")
    public ResponseEntity<?> obtenerTodas() {
        try {
            return ResponseEntity.ok(asistenciaRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener asistencias: " + e.getMessage());
        }
    }
}