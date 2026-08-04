package com.example.auditoriabd.repository;

import com.example.auditoriabd.entity.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {
    List<Respuesta> findByAuditoria_IdAuditoria(Integer idAuditoria);
    Optional<Respuesta> findByAuditoria_IdAuditoriaAndPregunta_IdPregunta(Integer idAuditoria, Integer idPregunta);
}
