package com.dn.sistema_asistencia.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "bitacora_muro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BitacoraMuro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    // 🔥 Agregamos "bitacoras" (o el nombre de la lista inversa en Usuario) para evitar recursividad infinita
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fechaCreacion", "correo", "bitacoras"})
    private Usuario usuario;

    // Se mantiene igual, mapea directo con la columna "fecha" (tipo date en Postgres)
    private LocalDate fecha;

    @Column(name = "hora_registro")
    private LocalTime horaRegistro;

    @Column(columnDefinition = "TEXT")
    private String aprendizaje;

    @Column(name = "utilidad_conteo")
    private Integer utilidadConteo;
}