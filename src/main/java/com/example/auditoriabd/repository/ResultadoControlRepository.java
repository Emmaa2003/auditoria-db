package com.example.auditoriabd.repository;

import com.example.auditoriabd.entity.ResultadoControl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultadoControlRepository extends JpaRepository<ResultadoControl, Integer> {
    List<ResultadoControl> findByAuditoria_IdAuditoria(Integer idAuditoria);
    void deleteByAuditoria_IdAuditoria(Integer idAuditoria);
}
