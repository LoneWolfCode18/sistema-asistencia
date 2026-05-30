package com.dn.sistema_asistencia.service;

import com.dn.sistema_asistencia.entity.Asistencia;
import com.dn.sistema_asistencia.entity.Usuario;
import com.dn.sistema_asistencia.repository.AsistenciaRepository;
import com.dn.sistema_asistencia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AsistenciaService {

    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;

    @Transactional
    public String registrarAsistencia(String telegramUser) {
        Usuario usuario = usuarioRepository.findByTelegramUser(telegramUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con Telegram User: " + telegramUser));

        LocalDate hoy = LocalDate.now();
        
        return asistenciaRepository.findByUsuarioIdAndFecha(usuario.getId(), hoy)
                .map(asistencia -> {
                    if (asistencia.getHoraSalida() != null) {
                        throw new RuntimeException("Ya completó su jornada laboral por el día de hoy.");
                    }
                    asistencia.setHoraSalida(LocalTime.now());
                    asistenciaRepository.save(asistencia);
                    return "Salida registrada exitosamente";
                })
                .orElseGet(() -> {
                    Asistencia nuevaAsistencia = Asistencia.builder()
                            .usuario(usuario)
                            .fecha(hoy)
                            .horaEntrada(LocalTime.now())
                            .estadoAsistencia("REGISTRADO")
                            .build();
                    asistenciaRepository.save(nuevaAsistencia);
                    return "Entrada registrada exitosamente";
                });
    }
}
