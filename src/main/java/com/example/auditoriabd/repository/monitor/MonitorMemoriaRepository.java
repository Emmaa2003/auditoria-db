package com.example.auditoriabd.repository.monitor;

import com.example.auditoriabd.entity.monitor.MonitorMemoria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitorMemoriaRepository extends JpaRepository<MonitorMemoria, Integer> {

    List<MonitorMemoria> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
