package com.example.auditoriabd.service;

import com.example.auditoriabd.dto.PublicoMetricasView;
import com.example.auditoriabd.entity.Control;
import com.example.auditoriabd.entity.EstadoAuditoria;
import com.example.auditoriabd.entity.ResultadoAuditoria;
import com.example.auditoriabd.repository.AuditoriaRepository;
import com.example.auditoriabd.repository.ControlRepository;
import com.example.auditoriabd.repository.OrganizacionRepository;
import com.example.auditoriabd.repository.ResultadoAuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Métricas agregadas y anónimas para la landing pública ("/"), calculadas
 * en vivo sobre lo que ya hay registrado en la base de datos. No expone
 * datos de organizaciones ni de auditorías individuales.
 */
@Service
public class PublicoDashboardService {

    private final OrganizacionRepository organizacionRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final ControlRepository controlRepository;
    private final ResultadoAuditoriaRepository resultadoAuditoriaRepository;

    public PublicoDashboardService(OrganizacionRepository organizacionRepository,
                                    AuditoriaRepository auditoriaRepository,
                                    ControlRepository controlRepository,
                                    ResultadoAuditoriaRepository resultadoAuditoriaRepository) {
        this.organizacionRepository = organizacionRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.controlRepository = controlRepository;
        this.resultadoAuditoriaRepository = resultadoAuditoriaRepository;
    }

    @Transactional(readOnly = true)
    public PublicoMetricasView obtenerMetricas() {
        PublicoMetricasView view = new PublicoMetricasView();

        view.setOrganizacionesRegistradas(organizacionRepository.count());
        view.setAuditoriasFinalizadas(auditoriaRepository.countByEstado(EstadoAuditoria.FINALIZADA));
        view.setAuditoriasEnProgreso(auditoriaRepository.countByEstado(EstadoAuditoria.EN_PROGRESO));

        List<Control> controles = controlRepository.findAll();
        view.setTotalControlesEvaluados(controles.size());
        view.setTotalDominios(controles.stream().map(Control::getDominio).distinct().count());

        Map<String, Long> distribucion = new LinkedHashMap<>();
        distribucion.put("Bajo", 0L);
        distribucion.put("Medio", 0L);
        distribucion.put("Alto", 0L);
        distribucion.put("Crítico", 0L);
        view.setDistribucionRiesgo(distribucion);

        List<ResultadoAuditoria> resultados = resultadoAuditoriaRepository.findAll();
        if (resultados.isEmpty()) {
            view.setHayResultados(false);
            return view;
        }

        view.setHayResultados(true);

        BigDecimal sumaMadurez = BigDecimal.ZERO;
        BigDecimal sumaRiesgo = BigDecimal.ZERO;
        for (ResultadoAuditoria resultado : resultados) {
            sumaMadurez = sumaMadurez.add(resultado.getMadurezGeneral());
            sumaRiesgo = sumaRiesgo.add(resultado.getIndiceRiesgoGeneral());
            String nivel = RiesgoUtil.nivelRiesgoTexto(resultado.getIndiceRiesgoGeneral());
            distribucion.merge(nivel, 1L, Long::sum);
        }

        BigDecimal cantidad = BigDecimal.valueOf(resultados.size());
        BigDecimal madurezPromedio = sumaMadurez.divide(cantidad, 2, RoundingMode.HALF_UP);
        BigDecimal riesgoPromedio = sumaRiesgo.divide(cantidad, 2, RoundingMode.HALF_UP);

        view.setMadurezPromedio(madurezPromedio);
        view.setRiesgoPromedio(riesgoPromedio);
        view.setRiesgoPromedioTexto(RiesgoUtil.nivelRiesgoTexto(riesgoPromedio));
        view.setRiesgoPromedioCss(RiesgoUtil.nivelRiesgoCss(riesgoPromedio));

        return view;
    }
}
