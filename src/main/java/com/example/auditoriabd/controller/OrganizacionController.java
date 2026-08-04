package com.example.auditoriabd.controller;

import com.example.auditoriabd.dto.OrganizacionForm;
import com.example.auditoriabd.entity.Organizacion;
import com.example.auditoriabd.service.AuditoriaService;
import com.example.auditoriabd.service.OrganizacionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/organizaciones")
public class OrganizacionController {

    private final OrganizacionService organizacionService;
    private final AuditoriaService auditoriaService;

    public OrganizacionController(OrganizacionService organizacionService, AuditoriaService auditoriaService) {
        this.organizacionService = organizacionService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("organizaciones", organizacionService.listarTodas());
        return "organizacion/lista";
    }

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        model.addAttribute("organizacionForm", new OrganizacionForm());
        return "organizacion/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Integer id, Model model) {
        Organizacion organizacion = organizacionService.buscarPorId(id);
        model.addAttribute("organizacionForm", organizacionService.aFormulario(organizacion));
        return "organizacion/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("organizacionForm") OrganizacionForm form,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "organizacion/form";
        }
        organizacionService.guardar(form);
        redirectAttributes.addFlashAttribute("mensaje", "Organización guardada correctamente.");
        return "redirect:/organizaciones";
    }

    @GetMapping("/{id}/historial")
    public String historial(@PathVariable Integer id, Model model) {
        Organizacion organizacion = organizacionService.buscarPorId(id);
        model.addAttribute("organizacion", organizacion);
        model.addAttribute("auditorias", auditoriaService.listarPorOrganizacion(id));
        return "organizacion/historial";
    }
}
