package com.dn.sistema_asistencia.repository;

import com.dn.sistema_asistencia.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // 🎯 Forzamos la consulta directa usando el nombre exacto de la columna en Supabase
    @Query(value = "SELECT * FROM usuarios WHERE telegram_user = :telegramUser LIMIT 1", nativeQuery = true)
    Optional<Usuario> findByTelegramUser(@Param("telegramUser") String telegramUser);
}