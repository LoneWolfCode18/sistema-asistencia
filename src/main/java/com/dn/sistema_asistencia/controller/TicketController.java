package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.Ticket;
import com.dn.sistema_asistencia.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // Endpoint para ver los tickets asignados a un programador por su ID
    @GetMapping("/usuario/{usuarioId}")
    public List<Ticket> obtenerTicketsPorUsuario(@PathVariable Long usuarioId) {
        return ticketRepository.findByUsuarioId(usuarioId);
    }
}