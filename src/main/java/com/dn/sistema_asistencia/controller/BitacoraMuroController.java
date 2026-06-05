package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.BitacoraMuro;
import com.dn.sistema_asistencia.repository.BitacoraMuroRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/muro")
@CrossOrigin(origins = "*") // Para que tu JS del frontend no tenga problemas de CORS
public class BitacoraMuroController {

    private final BitacoraMuroRepository bitacoraMuroRepository;

    public BitacoraMuroController(BitacoraMuroRepository bitacoraMuroRepository) {
        this.bitacoraMuroRepository = bitacoraMuroRepository;
    }

    @GetMapping("/listar")
    public ResponseEntity<List<BitacoraMuro>> listarPublicaciones() {
        // Jalamos todas las bitácoras guardadas desde Telegram directo de Supabase
        List<BitacoraMuro> publicaciones = bitacoraMuroRepository.findAll();
        return ResponseEntity.ok(publicaciones);
    }
}