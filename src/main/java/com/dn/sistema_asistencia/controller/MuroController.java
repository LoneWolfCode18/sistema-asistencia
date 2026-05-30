package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.BitacoraMuro;
import com.dn.sistema_asistencia.repository.BitacoraMuroRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/muro")
public class MuroController {

    private final BitacoraMuroRepository bitacoraMuroRepository;

    public MuroController(BitacoraMuroRepository bitacoraMuroRepository) {
        this.bitacoraMuroRepository = bitacoraMuroRepository;
    }

    // Para ver todas las actividades publicadas por los devs remotos
    @GetMapping("/publicaciones")
    public List<BitacoraMuro> obtenerFeedMuro() {
        return bitacoraMuroRepository.findAll();
    }

    // Para que un dev publique lo que está haciendo desde su casa
    @PostMapping("/publicar")
    public BitacoraMuro publicarAvance(@RequestBody BitacoraMuro nuevaBitacora) {
        return bitacoraMuroRepository.save(nuevaBitacora);
    }
}