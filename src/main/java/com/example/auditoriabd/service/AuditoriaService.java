package com.example.auditoriabd.service;

import com.example.auditoriabd.dto.CuestionarioForm;
import com.example.auditoriabd.dto.GrupoControlCuestionario;
import com.example.auditoriabd.dto.PreguntaIndexada;
import com.example.auditoriabd.dto.RespuestaForm;
import com.example.auditoriabd.entity.Auditoria;
import com.example.auditoriabd.entity.Control;
import com.example.auditoriabd.entity.EstadoAuditoria;
import com.example.auditoriabd.entity.Organizacion;
import com.example.auditoriabd.entity.Pregunta;
import com.example.auditoriabd.entity.Respuesta;
import com.example.auditoriabd.entity.Usuario;
import com.example.auditoriabd.entity.ValorRespuesta;
import com.example.auditoriabd.repository.AuditoriaRepository;
import com.example.auditoriabd.repository.ControlRepository;
import com.example.auditoriabd.repository.PreguntaRepository;
import com.example.auditoriabd.repository.RespuestaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final ControlRepository controlRepository;
    private final RespuestaRepository respuestaRepository;
    private final PreguntaRepository preguntaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, ControlRepository controlRepository,
                             RespuestaRepository respuestaRepository, PreguntaRepository preguntaRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.controlRepository = controlRepository;
        this.respuestaRepository = respuestaRepository;
        this.preguntaRepository = preguntaRepository;
    }

    public List<Auditoria> listarPorOrganizacion(Integer idOrganizacion) {
        return auditoriaRepository.findByOrganizacion_IdOrganizacionOrderByFechaAuditoriaDesc(idOrganizacion);
    }

    public Auditoria buscarPorId(Integer id) {
        return auditoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Auditoría no encontrada: " + id));
    }

    public Auditoria crear(Organizacion organizacion, LocalDate fechaAuditoria, Usuario auditor) {
        Auditoria auditoria = new Auditoria();
        auditoria.setOrganizacion(organizacion);
        auditoria.setFechaAuditoria(fechaAuditoria);
        auditoria.setAuditor(auditor);
        auditoria.setEstado(EstadoAuditoria.EN_PROGRESO);
        return auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public List<GrupoControlCuestionario> construirGrupos() {
        List<Control> controles = controlRepository.findAllByOrderByCodigoAsc();
        List<GrupoControlCuestionario> grupos = new ArrayList<>();
        int indice = 0;
        for (Control control : controles) {
            List<PreguntaIndexada> preguntasIndexadas = new ArrayList<>();
            for (Pregunta pregunta : control.getPreguntas()) {
                preguntasIndexadas.add(new PreguntaIndexada(pregunta, indice));
                indice++;
            }
            grupos.add(new GrupoControlCuestionario(control, preguntasIndexadas));
        }
        return grupos;
    }

    public CuestionarioForm construirFormulario(Integer idAuditoria, List<GrupoControlCuestionario> grupos) {
        Map<Integer, Respuesta> respuestasExistentes = new HashMap<>();
        for (Respuesta respuesta : respuestaRepository.findByAuditoria_IdAuditoria(idAuditoria)) {
            respuestasExistentes.put(respuesta.getPregunta().getIdPregunta(), respuesta);
        }

        CuestionarioForm form = new CuestionarioForm();
        List<RespuestaForm> respuestas = new ArrayList<>();
        for (GrupoControlCuestionario grupo : grupos) {
            for (PreguntaIndexada preguntaIndexada : grupo.getPreguntas()) {
                Pregunta pregunta = preguntaIndexada.getPregunta();
                RespuestaForm respuestaForm = new RespuestaForm();
                respuestaForm.setIdPregunta(pregunta.getIdPregunta());
                Respuesta existente = respuestasExistentes.get(pregunta.getIdPregunta());
                if (existente != null) {
                    respuestaForm.setValor(existente.getValor().name());
                    respuestaForm.setObservacion(existente.getObservacion());
                    respuestaForm.setEvidencia(existente.getEvidencia());
                }
                respuestas.add(respuestaForm);
            }
        }
        form.setRespuestas(respuestas);
        return form;
    }

    @Transactional
    public void guardarRespuestas(Integer idAuditoria, CuestionarioForm form) {
        Auditoria auditoria = buscarPorId(idAuditoria);
        if (auditoria.getEstado() != EstadoAuditoria.EN_PROGRESO) {
            throw new IllegalStateException("La auditoría ya fue finalizada y no admite más cambios.");
        }

        for (RespuestaForm respuestaForm : form.getRespuestas()) {
            if (respuestaForm.getIdPregunta() == null || respuestaForm.getValor() == null
                    || respuestaForm.getValor().isBlank()) {
                continue;
            }
            Respuesta respuesta = respuestaRepository
                    .findByAuditoria_IdAuditoriaAndPregunta_IdPregunta(idAuditoria, respuestaForm.getIdPregunta())
                    .orElseGet(() -> {
                        Respuesta nueva = new Respuesta();
                        nueva.setAuditoria(auditoria);
                        nueva.setPregunta(preguntaRepository.getReferenceById(respuestaForm.getIdPregunta()));
                        return nueva;
                    });
            respuesta.setValor(ValorRespuesta.valueOf(respuestaForm.getValor()));
            respuesta.setObservacion(respuestaForm.getObservacion());
            respuesta.setEvidencia(respuestaForm.getEvidencia());
            respuestaRepository.save(respuesta);
        }
    }
}
