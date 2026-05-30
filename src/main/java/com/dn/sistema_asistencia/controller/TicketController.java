package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.Ticket;
import com.dn.sistema_asistencia.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/todos")
    public List<Ticket> obtenerTodos() {
        return ticketRepository.findAll();
    }
}