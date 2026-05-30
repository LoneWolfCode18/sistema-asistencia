package com.dn.sistema_asistencia.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <--- Importa esto

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // <--- Agrégalo aquí
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true)
    private String correo;

    @Column(name = "telegram_user")
    private String telegramUser;

    private String estado = "ACTIVO";

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
