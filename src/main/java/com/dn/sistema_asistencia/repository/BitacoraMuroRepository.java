package com.dn.sistema_asistencia.repository;

import com.dn.sistema_asistencia.entity.BitacoraMuro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraMuroRepository extends JpaRepository<BitacoraMuro, Long> {
    // Traerá todas las bitácoras ordenadas para el feed
}