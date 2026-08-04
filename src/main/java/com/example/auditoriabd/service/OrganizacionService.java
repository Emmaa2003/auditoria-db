package com.example.auditoriabd.service;

import com.example.auditoriabd.dto.OrganizacionForm;
import com.example.auditoriabd.entity.Organizacion;
import com.example.auditoriabd.repository.OrganizacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizacionService {

    private final OrganizacionRepository organizacionRepository;

    public OrganizacionService(OrganizacionRepository organizacionRepository) {
        this.organizacionRepository = organizacionRepository;
    }

    public List<Organizacion> listarTodas() {
        return organizacionRepository.findAll();
    }

    public Organizacion buscarPorId(Integer id) {
        return organizacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada: " + id));
    }

    public Organizacion guardar(OrganizacionForm form) {
        Organizacion organizacion;
        if (form.getIdOrganizacion() != null) {
            organizacion = buscarPorId(form.getIdOrganizacion());
        } else {
            organizacion = new Organizacion();
        }
        organizacion.setNombre(form.getNombre());
        organizacion.setAreaEvaluada(form.getAreaEvaluada());
        organizacion.setAdministradorBd(form.getAdministradorBd());
        return organizacionRepository.save(organizacion);
    }

    public OrganizacionForm aFormulario(Organizacion organizacion) {
        OrganizacionForm form = new OrganizacionForm();
        form.setIdOrganizacion(organizacion.getIdOrganizacion());
        form.setNombre(organizacion.getNombre());
        form.setAreaEvaluada(organizacion.getAreaEvaluada());
        form.setAdministradorBd(organizacion.getAdministradorBd());
        return form;
    }
}
