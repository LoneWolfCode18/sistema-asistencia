package com.dn.sistema_asistencia.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    // 🔥 Clave para evitar el Error 500 al listar los Tickets
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fechaCreacion", "correo"})
    private Usuario usuario;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado_ticket")
    private String estadoTicket;

    @Column(name = "url_evidencia")
    private String urlEvidencia;

    @Column(name = "fecha_asignacion")
    private java.time.OffsetDateTime fechaAsignacion;
}