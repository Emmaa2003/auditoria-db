package com.example.auditoriabd.service;

import com.example.auditoriabd.dto.ControlForm;
import com.example.auditoriabd.dto.PreguntaForm;
import com.example.auditoriabd.entity.Control;
import com.example.auditoriabd.entity.Pregunta;
import com.example.auditoriabd.repository.ControlRepository;
import com.example.auditoriabd.repository.PreguntaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogoService {

    private final ControlRepository controlRepository;
    private final PreguntaRepository preguntaRepository;

    public CatalogoService(ControlRepository controlRepository, PreguntaRepository preguntaRepository) {
        this.controlRepository = controlRepository;
        this.preguntaRepository = preguntaRepository;
    }

    public List<Control> listarControles() {
        return controlRepository.findAllByOrderByCodigoAsc();
    }

    public Control buscarControl(Integer id) {
        return controlRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Control no encontrado: " + id));
    }

    public List<Pregunta> preguntasDeControl(Integer idControl) {
        return preguntaRepository.findByControl_IdControlOrderByOrdenAsc(idControl);
    }

    public ControlForm aFormulario(Control control) {
        ControlForm form = new ControlForm();
        form.setIdControl(control.getIdControl());
        form.setCodigo(control.getCodigo());
        form.setNombre(control.getNombre());
        form.setObjetivo(control.getObjetivo());
        form.setDescripcion(control.getDescripcion());
        form.setPeso(control.getPeso());
        form.setAfectaC(control.isAfectaC());
        form.setAfectaI(control.isAfectaI());
        form.setAfectaD(control.isAfectaD());
        return form;
    }

    public Control actualizarControl(ControlForm form) {
        Control control = buscarControl(form.getIdControl());
        control.setNombre(form.getNombre());
        control.setObjetivo(form.getObjetivo());
        control.setDescripcion(form.getDescripcion());
        control.setPeso(form.getPeso());
        control.setAfectaC(form.isAfectaC());
        control.setAfectaI(form.isAfectaI());
        control.setAfectaD(form.isAfectaD());
        return controlRepository.save(control);
    }

    public Pregunta guardarPregunta(PreguntaForm form) {
        Pregunta pregunta;
        if (form.getIdPregunta() != null) {
            pregunta = preguntaRepository.findById(form.getIdPregunta())
                    .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada: " + form.getIdPregunta()));
        } else {
            pregunta = new Pregunta();
            pregunta.setControl(buscarControl(form.getIdControl()));
        }
        pregunta.setTexto(form.getTexto());
        pregunta.setOrden(form.getOrden());
        return preguntaRepository.save(pregunta);
    }
}
