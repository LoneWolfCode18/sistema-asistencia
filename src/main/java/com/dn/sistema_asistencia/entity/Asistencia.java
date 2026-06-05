package com.dn.sistema_asistencia.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "asistencias", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "fecha"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    // 🔥 Agregado aquí también para que el dashboard pinte la asistencia sin romperse
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fechaCreacion", "correo", "asistencias", "tickets", "bitacoras"})
    private Usuario usuario;

    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "estado_asistencia")
    private String estadoAsistencia;


}