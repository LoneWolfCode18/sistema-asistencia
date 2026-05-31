package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.Ticket;
import com.dn.sistema_asistencia.repository.TicketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*") // Permite que tu Dashboard (frontend) consuma estos datos
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // Endpoint para listar todos los tickets
    @GetMapping("/todos")
    public List<Ticket> obtenerTodos() {
        return ticketRepository.findAll();
    }

    // Endpoint para resolver tickets (Cambia estado a RESUELTO)
    @PutMapping("/resolver/{id}")
    public ResponseEntity<Ticket> resolverTicket(@PathVariable Long id) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    // Usamos setEstadoTicket porque es el nombre del campo en tu entidad
                    ticket.setEstadoTicket("RESUELTO");
                    Ticket ticketActualizado = ticketRepository.save(ticket);
                    return ResponseEntity.ok(ticketActualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}