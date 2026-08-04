package com.example.auditoriabd.controller;

import com.example.auditoriabd.dto.ControlForm;
import com.example.auditoriabd.dto.PreguntaForm;
import com.example.auditoriabd.entity.Control;
import com.example.auditoriabd.service.CatalogoService;
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
@RequestMapping("/admin/catalogo")
public class AdminCatalogoController {

    private final CatalogoService catalogoService;

    public AdminCatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("controles", catalogoService.listarControles());
        return "admin/catalogo";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        Control control = catalogoService.buscarControl(id);
        model.addAttribute("control", control);
        model.addAttribute("controlForm", catalogoService.aFormulario(control));
        model.addAttribute("preguntas", catalogoService.preguntasDeControl(id));
        model.addAttribute("nuevaPreguntaForm", nuevaPreguntaForm(id));
        return "admin/control-detalle";
    }

    @PostMapping("/{id}/guardar")
    public String guardarControl(@PathVariable Integer id, @Valid @ModelAttribute("controlForm") ControlForm form,
                                  BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("control", catalogoService.buscarControl(id));
            model.addAttribute("preguntas", catalogoService.preguntasDeControl(id));
            model.addAttribute("nuevaPreguntaForm", nuevaPreguntaForm(id));
            return "admin/control-detalle";
        }
        form.setIdControl(id);
        catalogoService.actualizarControl(form);
        redirectAttributes.addFlashAttribute("mensaje", "Control actualizado correctamente.");
        return "redirect:/admin/catalogo/" + id;
    }

    @PostMapping("/preguntas/guardar")
    public String guardarPregunta(@Valid @ModelAttribute("preguntaForm") PreguntaForm form,
                                   BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Integer idControl = form.getIdControl();
        if (!bindingResult.hasErrors()) {
            catalogoService.guardarPregunta(form);
            redirectAttributes.addFlashAttribute("mensaje", "Pregunta guardada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo guardar la pregunta: revise los datos.");
        }
        return "redirect:/admin/catalogo/" + idControl;
    }

    private PreguntaForm nuevaPreguntaForm(Integer idControl) {
        PreguntaForm form = new PreguntaForm();
        form.setIdControl(idControl);
        form.setOrden(catalogoService.preguntasDeControl(idControl).size() + 1);
        return form;
    }
}
