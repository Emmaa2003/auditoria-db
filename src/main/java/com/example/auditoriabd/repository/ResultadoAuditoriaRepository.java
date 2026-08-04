package com.example.auditoriabd.repository;

import com.example.auditoriabd.entity.ResultadoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultadoAuditoriaRepository extends JpaRepository<ResultadoAuditoria, Integer> {
    Optional<ResultadoAuditoria> findByAuditoria_IdAuditoria(Integer idAuditoria);
    void deleteByAuditoria_IdAuditoria(Integer idAuditoria);
}
