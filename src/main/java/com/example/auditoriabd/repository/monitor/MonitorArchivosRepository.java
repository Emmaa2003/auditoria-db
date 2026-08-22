package com.example.auditoriabd.repository.monitor;

import com.example.auditoriabd.entity.monitor.MonitorArchivos;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitorArchivosRepository extends JpaRepository<MonitorArchivos, Integer> {

    List<MonitorArchivos> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
