package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.BitacoraMuro;
import com.dn.sistema_asistencia.repository.BitacoraMuroRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/muro")
@CrossOrigin(origins = "*") // 🔥 Con esto, tu VSC podrá pedirle datos sin problemas
public class MuroController {

    private final BitacoraMuroRepository muroRepository;

    public MuroController(BitacoraMuroRepository muroRepository) {
        this.muroRepository = muroRepository;
    }

    // El Dashboard en VSC llamará a esto para listar todos los aprendizajes
    @GetMapping("/listar")
    public List<BitacoraMuro> listarMuro() {
        return muroRepository.findAll();
    }
}