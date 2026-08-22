package com.example.auditoriabd.repository.monitor;

import com.example.auditoriabd.entity.monitor.MonitorIndice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitorIndiceRepository extends JpaRepository<MonitorIndice, Integer> {

    List<MonitorIndice> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
