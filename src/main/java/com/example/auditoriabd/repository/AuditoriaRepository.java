package com.example.auditoriabd.repository;

import com.example.auditoriabd.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
    List<Auditoria> findByOrganizacion_IdOrganizacionOrderByFechaAuditoriaDesc(Integer idOrganizacion);
    List<Auditoria> findAllByOrderByFechaCreacionDesc();
}
