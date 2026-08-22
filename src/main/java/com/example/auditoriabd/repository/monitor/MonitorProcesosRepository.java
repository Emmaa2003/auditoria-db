package com.example.auditoriabd.repository.monitor;

import com.example.auditoriabd.entity.monitor.MonitorProcesos;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitorProcesosRepository extends JpaRepository<MonitorProcesos, Integer> {

    List<MonitorProcesos> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
