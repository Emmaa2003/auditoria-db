package com.example.auditoriabd.service;

import com.example.auditoriabd.entity.Auditoria;
import com.example.auditoriabd.entity.Control;
import com.example.auditoriabd.entity.EstadoAuditoria;
import com.example.auditoriabd.entity.Pregunta;
import com.example.auditoriabd.entity.Respuesta;
import com.example.auditoriabd.entity.ResultadoAuditoria;
import com.example.auditoriabd.entity.ResultadoControl;
import com.example.auditoriabd.entity.ValorRespuesta;
import com.example.auditoriabd.repository.AuditoriaRepository;
import com.example.auditoriabd.repository.ControlRepository;
import com.example.auditoriabd.repository.ResultadoAuditoriaRepository;
import com.example.auditoriabd.repository.ResultadoControlRepository;
import com.example.auditoriabd.repository.RespuestaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalculoAuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final ControlRepository controlRepository;
    private final RespuestaRepository respuestaRepository;
    private final ResultadoControlRepository resultadoControlRepository;
    private final ResultadoAuditoriaRepository resultadoAuditoriaRepository;

    public CalculoAuditoriaService(AuditoriaRepository auditoriaRepository, ControlRepository controlRepository,
                                    RespuestaRepository respuestaRepository,
                                    ResultadoControlRepository resultadoControlRepository,
                                    ResultadoAuditoriaRepository resultadoAuditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.controlRepository = controlRepository;
        this.respuestaRepository = respuestaRepository;
        this.resultadoControlRepository = resultadoControlRepository;
        this.resultadoAuditoriaRepository = resultadoAuditoriaRepository;
    }

    @Transactional
    public void finalizar(Integer idAuditoria) {
        Auditoria auditoria = auditoriaRepository.findById(idAuditoria)
                .orElseThrow(() -> new EntityNotFoundException("Auditoría no encontrada: " + idAuditoria));

        List<Respuesta> respuestas = respuestaRepository.findByAuditoria_IdAuditoria(idAuditoria);
        Map<Integer, List<Respuesta>> respuestasPorControl = new HashMap<>();
        for (Respuesta respuesta : respuestas) {
            Pregunta pregunta = respuesta.getPregunta();
            Integer idControl = pregunta.getControl().getIdControl();
            respuestasPorControl.computeIfAbsent(idControl, k -> new ArrayList<>()).add(respuesta);
        }

        resultadoControlRepository.deleteByAuditoria_IdAuditoria(idAuditoria);
        resultadoAuditoriaRepository.deleteByAuditoria_IdAuditoria(idAuditoria);

        List<Control> controles = controlRepository.findAll();
        List<ResultadoControl> resultadosGuardados = new ArrayList<>();

        for (Control control : controles) {
            List<Respuesta> respuestasControl = respuestasPorControl.getOrDefault(control.getIdControl(), List.of());

            long countSI = respuestasControl.stream().filter(r -> r.getValor() == ValorRespuesta.SI).count();
            long countNoNA = respuestasControl.stream().filter(r -> r.getValor() != ValorRespuesta.NO_APLICA).count();

            if (countNoNA == 0) {
                // Todas las preguntas fueron NO_APLICA (o no se respondieron): se excluye el control.
                continue;
            }

            BigDecimal porcentaje = BigDecimal.valueOf(countSI)
                    .divide(BigDecimal.valueOf(countNoNA), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            short nivelMadurez = RiesgoUtil.calcularNivelMadurez(porcentaje);
            BigDecimal riesgoControl = RiesgoUtil.calcularRiesgoControl(nivelMadurez, control.getPeso());

            ResultadoControl resultado = new ResultadoControl();
            resultado.setAuditoria(auditoria);
            resultado.setControl(control);
            resultado.setPorcentajeCumplimiento(porcentaje);
            resultado.setNivelMadurez(nivelMadurez);
            resultado.setRiesgoControl(riesgoControl);
            resultadosGuardados.add(resultadoControlRepository.save(resultado));
        }

        ResultadoAuditoria resultadoAuditoria = new ResultadoAuditoria();
        resultadoAuditoria.setAuditoria(auditoria);

        if (resultadosGuardados.isEmpty()) {
            resultadoAuditoria.setMadurezGeneral(BigDecimal.ZERO);
            resultadoAuditoria.setRiesgoConfidencialidad(BigDecimal.ZERO);
            resultadoAuditoria.setRiesgoIntegridad(BigDecimal.ZERO);
            resultadoAuditoria.setRiesgoDisponibilidad(BigDecimal.ZERO);
            resultadoAuditoria.setIndiceRiesgoGeneral(BigDecimal.ZERO);
        } else {
            BigDecimal madurezGeneral = promedio(resultadosGuardados.stream()
                    .map(r -> BigDecimal.valueOf(r.getNivelMadurez())).toList()).setScale(2, RoundingMode.HALF_UP);

            BigDecimal riesgoC = promedioRiesgoPorDimension(resultadosGuardados, ResultadoControl::getControl, true, false, false);
            BigDecimal riesgoI = promedioRiesgoPorDimension(resultadosGuardados, ResultadoControl::getControl, false, true, false);
            BigDecimal riesgoD = promedioRiesgoPorDimension(resultadosGuardados, ResultadoControl::getControl, false, false, true);

            BigDecimal riesgoCPct = RiesgoUtil.normalizarRiesgoPorcentaje(riesgoC);
            BigDecimal riesgoIPct = RiesgoUtil.normalizarRiesgoPorcentaje(riesgoI);
            BigDecimal riesgoDPct = RiesgoUtil.normalizarRiesgoPorcentaje(riesgoD);

            BigDecimal indiceGeneral = promedio(List.of(riesgoCPct, riesgoIPct, riesgoDPct)).setScale(2, RoundingMode.HALF_UP);

            resultadoAuditoria.setMadurezGeneral(madurezGeneral);
            resultadoAuditoria.setRiesgoConfidencialidad(riesgoCPct);
            resultadoAuditoria.setRiesgoIntegridad(riesgoIPct);
            resultadoAuditoria.setRiesgoDisponibilidad(riesgoDPct);
            resultadoAuditoria.setIndiceRiesgoGeneral(indiceGeneral);
        }

        resultadoAuditoriaRepository.save(resultadoAuditoria);

        auditoria.setEstado(EstadoAuditoria.FINALIZADA);
        auditoriaRepository.save(auditoria);
    }

    private BigDecimal promedioRiesgoPorDimension(List<ResultadoControl> resultados,
                                                   java.util.function.Function<ResultadoControl, Control> controlFn,
                                                   boolean c, boolean i, boolean d) {
        List<BigDecimal> valores = resultados.stream()
                .filter(r -> {
                    Control control = controlFn.apply(r);
                    if (c) return control.isAfectaC();
                    if (i) return control.isAfectaI();
                    return control.isAfectaD();
                })
                .map(ResultadoControl::getRiesgoControl)
                .toList();
        if (valores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return promedio(valores);
    }

    private BigDecimal promedio(List<BigDecimal> valores) {
        BigDecimal suma = BigDecimal.ZERO;
        for (BigDecimal valor : valores) {
            suma = suma.add(valor);
        }
        return suma.divide(BigDecimal.valueOf(valores.size()), 6, RoundingMode.HALF_UP);
    }
}
