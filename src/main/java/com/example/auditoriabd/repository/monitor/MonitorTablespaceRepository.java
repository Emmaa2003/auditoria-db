package com.example.auditoriabd.repository.monitor;

import com.example.auditoriabd.entity.monitor.MonitorTablespace;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitorTablespaceRepository extends JpaRepository<MonitorTablespace, Integer> {

    /** Ultimas N filas (de TODOS los tablespaces mezclados); se agrupan por nombre en el servicio. */
    List<MonitorTablespace> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
