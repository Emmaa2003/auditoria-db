package com.example.auditoriabd.repository.monitor;

import com.example.auditoriabd.entity.monitor.MonitorAlerta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonitorAlertaRepository extends JpaRepository<MonitorAlerta, Integer> {

    List<MonitorAlerta> findAllByOrderByFechaHoraDesc(Pageable pageable);

    /** La ultima fila para esta condicion (componente+variable), sin importar el nivel - para
     * decidir si el ciclo actual continua la misma condicion (se actualiza) o es un incidente
     * nuevo (se inserta una fila aparte). Ver comentario de clase en {@link MonitorAlerta}. */
    Optional<MonitorAlerta> findTopByComponenteAndVariableOrderByFechaHoraDesc(String componente, String variable);
}
