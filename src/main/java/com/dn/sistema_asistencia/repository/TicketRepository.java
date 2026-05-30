package com.dn.sistema_asistencia.repository;

import com.dn.sistema_asistencia.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Este método clave buscará solo los tickets asignados a un programador específico
    List<Ticket> findByUsuarioId(Long usuarioId);
}