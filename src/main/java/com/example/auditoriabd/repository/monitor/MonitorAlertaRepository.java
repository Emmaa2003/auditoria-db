package com.example.auditoriabd.repository.monitor;

import com.example.auditoriabd.entity.monitor.MonitorAlerta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitorAlertaRepository extends JpaRepository<MonitorAlerta, Integer> {

    List<MonitorAlerta> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
