package com.example.auditoriabd.repository;

import com.example.auditoriabd.entity.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {
    List<Pregunta> findByControl_IdControlOrderByOrdenAsc(Integer idControl);
    List<Pregunta> findAllByOrderByControl_IdControlAscOrdenAsc();
}
