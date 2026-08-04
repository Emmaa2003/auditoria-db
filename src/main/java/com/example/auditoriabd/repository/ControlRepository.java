package com.example.auditoriabd.repository;

import com.example.auditoriabd.entity.Control;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ControlRepository extends JpaRepository<Control, Integer> {
    Optional<Control> findByCodigo(String codigo);
    List<Control> findAllByOrderByCodigoAsc();
}
