package com.example.auditoriabd.service;

import com.example.auditoriabd.dto.ReporteAuditoriaView;
import com.example.auditoriabd.dto.ResultadoControlView;
import com.example.auditoriabd.dto.ResultadoDominioView;
import com.example.auditoriabd.entity.Auditoria;
import com.example.auditoriabd.entity.Control;
import com.example.auditoriabd.entity.ResultadoAuditoria;
import com.example.auditoriabd.entity.ResultadoControl;
import com.example.auditoriabd.repository.AuditoriaRepository;
import com.example.auditoriabd.repository.ResultadoAuditoriaRepository;
import com.example.auditoriabd.repository.ResultadoControlRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private final AuditoriaRepository auditoriaRepository;
    private final ResultadoControlRepository resultadoControlRepository;
    private final ResultadoAuditoriaRepository resultadoAuditoriaRepository;

    public ReporteService(AuditoriaRepository auditoriaRepository, ResultadoControlRepository resultadoControlRepository,
                           ResultadoAuditoriaRepository resultadoAuditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.resultadoControlRepository = resultadoControlRepository;
        this.resultadoAuditoriaRepository = resultadoAuditoriaRepository;
    }

    @Transactional(readOnly = true)
    public ReporteAuditoriaView construirReporte(Integer idAuditoria) {
        Auditoria auditoria = auditoriaRepository.findById(idAuditoria)
                .orElseThrow(() -> new EntityNotFoundException("Auditoría no encontrada: " + idAuditoria));
        ResultadoAuditoria resultadoAuditoria = resultadoAuditoriaRepository.findByAuditoria_IdAuditoria(idAuditoria)
                .orElseThrow(() -> new EntityNotFoundException("La auditoría todavía no ha sido finalizada."));

        List<ResultadoControl> resultadosControl = resultadoControlRepository.findByAuditoria_IdAuditoria(idAuditoria);

        List<ResultadoControlView> controlViews = new ArrayList<>();
        for (ResultadoControl rc : resultadosControl) {
            Control control = rc.getControl();
            ResultadoControlView view = new ResultadoControlView();
            view.setCodigo(control.getCodigo());
            view.setNombre(control.getNombre());
            view.setDominio(control.getDominio());
            view.setPeso(control.getPeso().name());
            view.setNivelMadurez(rc.getNivelMadurez());
            view.setPorcentajeCumplimiento(rc.getPorcentajeCumplimiento());
            view.setRiesgoControl(rc.getRiesgoControl());
            BigDecimal riesgoPct = RiesgoUtil.normalizarRiesgoPorcentaje(rc.getRiesgoControl());
            view.setRiesgoControlPct(riesgoPct);
            view.setNivelRiesgoTexto(RiesgoUtil.nivelRiesgoTexto(riesgoPct));
            view.setNivelRiesgoCss(RiesgoUtil.nivelRiesgoCss(riesgoPct));
            controlViews.add(view);
        }
        controlViews.sort(Comparator.comparing(ResultadoControlView::getCodigo));

        Map<String, List<Short>> madurezPorDominio = new LinkedHashMap<>();
        for (ResultadoControlView view : controlViews) {
            madurezPorDominio.computeIfAbsent(view.getDominio(), k -> new ArrayList<>()).add(view.getNivelMadurez());
        }
        List<ResultadoDominioView> dominios = new ArrayList<>();
        for (Map.Entry<String, List<Short>> entry : madurezPorDominio.entrySet()) {
            BigDecimal suma = BigDecimal.ZERO;
            for (Short nivel : entry.getValue()) {
                suma = suma.add(BigDecimal.valueOf(nivel));
            }
            BigDecimal promedio = suma.divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
            dominios.add(new ResultadoDominioView(entry.getKey(), promedio));
        }

        List<ResultadoControlView> rankingMenorMadurez = new ArrayList<>(controlViews);
        rankingMenorMadurez.sort(Comparator.comparing(ResultadoControlView::getNivelMadurez));

        List<ResultadoControlView> rankingMayorRiesgo = new ArrayList<>(controlViews);
        rankingMayorRiesgo.sort(Comparator.comparing(ResultadoControlView::getRiesgoControlPct).reversed());

        List<String> recomendaciones = new ArrayList<>();
        for (ResultadoControlView view : rankingMenorMadurez.subList(0, Math.min(3, rankingMenorMadurez.size()))) {
            recomendaciones.add(RecomendacionUtil.recomendacionPara(view.getCodigo()));
        }

        ReporteAuditoriaView view = new ReporteAuditoriaView();
        view.setIdAuditoria(auditoria.getIdAuditoria());
        view.setOrganizacionNombre(auditoria.getOrganizacion().getNombre());
        view.setAreaEvaluada(auditoria.getOrganizacion().getAreaEvaluada());
        view.setFechaAuditoria(auditoria.getFechaAuditoria());
        view.setAuditorNombre(auditoria.getAuditor().getNombre());

        view.setMadurezGeneral(resultadoAuditoria.getMadurezGeneral());
        view.setRiesgoConfidencialidad(resultadoAuditoria.getRiesgoConfidencialidad());
        view.setRiesgoIntegridad(resultadoAuditoria.getRiesgoIntegridad());
        view.setRiesgoDisponibilidad(resultadoAuditoria.getRiesgoDisponibilidad());
        view.setIndiceRiesgoGeneral(resultadoAuditoria.getIndiceRiesgoGeneral());
        view.setNivelRiesgoGeneralTexto(RiesgoUtil.nivelRiesgoTexto(resultadoAuditoria.getIndiceRiesgoGeneral()));
        view.setNivelRiesgoGeneralCss(RiesgoUtil.nivelRiesgoCss(resultadoAuditoria.getIndiceRiesgoGeneral()));



        view.setControles(controlViews);
        view.setDominios(dominios);
        view.setRankingMenorMadurez(rankingMenorMadurez);
        view.setRankingMayorRiesgo(rankingMayorRiesgo);
        view.setRecomendaciones(recomendaciones);
        return view;
    }
}
