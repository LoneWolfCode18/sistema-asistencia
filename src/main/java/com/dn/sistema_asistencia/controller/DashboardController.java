package com.dn.sistema_asistencia.controller;

import com.dn.sistema_asistencia.entity.Asistencia;
import com.dn.sistema_asistencia.entity.BitacoraMuro;
import com.dn.sistema_asistencia.entity.Ticket;
import com.dn.sistema_asistencia.repository.AsistenciaRepository;
import com.dn.sistema_asistencia.repository.BitacoraMuroRepository;
import com.dn.sistema_asistencia.repository.TicketRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final AsistenciaRepository asistenciaRepository;
    private final TicketRepository ticketRepository;
    private final BitacoraMuroRepository bitacoraMuroRepository;

    // Inyectamos los 3 repositorios necesarios para el ecosistema corporativo
    public DashboardController(AsistenciaRepository asistenciaRepository,
                               TicketRepository ticketRepository,
                               BitacoraMuroRepository bitacoraMuroRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.ticketRepository = ticketRepository;
        this.bitacoraMuroRepository = bitacoraMuroRepository;
    }

    @GetMapping("/admin/dashboard")
    public String verDashboard(Model model) {
        // 1. Jalar datos reales de la Base de Datos
        List<Asistencia> listaAsistencias = asistenciaRepository.findAll();
        List<Ticket> listaTickets = ticketRepository.findAll();
        List<BitacoraMuro> listaMuro = bitacoraMuroRepository.findAll();

        // 2. Cálculos dinámicos en tiempo real para las tarjetas superiores
        long totalRegistros = listaAsistencias.size();

        long ingresosATiempo = listaAsistencias.stream()
                .filter(a -> "A TIEMPO".equalsIgnoreCase(a.getEstadoAsistencia()))
                .count();

        long totalTardanzas = listaAsistencias.stream()
                .filter(a -> "TARDANZA".equalsIgnoreCase(a.getEstadoAsistencia()))
                .count();

        // 3. Enviar TODAS las listas y contadores al HTML centralizado
        model.addAttribute("asistencias", listaAsistencias);
        model.addAttribute("tickets", listaTickets);
        model.addAttribute("publicacionesMuro", listaMuro);

        // Atributos de las tarjetas de resumen
        model.addAttribute("totalRegistros", totalRegistros);
        model.addAttribute("ingresosATiempo", ingresosATiempo);
        model.addAttribute("totalTardanzas", totalTardanzas);

        // Renderiza la plantilla templates/dashboard.html
        return "dashboard";
    }
}