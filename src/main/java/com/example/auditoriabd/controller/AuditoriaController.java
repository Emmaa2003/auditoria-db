package com.example.auditoriabd.controller;

import com.example.auditoriabd.dto.CuestionarioForm;
import com.example.auditoriabd.dto.GrupoControlCuestionario;
import com.example.auditoriabd.dto.NuevaAuditoriaForm;
import com.example.auditoriabd.entity.Auditoria;
import com.example.auditoriabd.entity.Organizacion;
import com.example.auditoriabd.service.AuditoriaService;
import com.example.auditoriabd.service.CalculoAuditoriaService;
import com.example.auditoriabd.service.OrganizacionService;
import com.example.auditoriabd.service.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/auditorias")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final OrganizacionService organizacionService;
    private final CalculoAuditoriaService calculoAuditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService, OrganizacionService organizacionService,
                                CalculoAuditoriaService calculoAuditoriaService) {
        this.auditoriaService = auditoriaService;
        this.organizacionService = organizacionService;
        this.calculoAuditoriaService = calculoAuditoriaService;
    }

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        model.addAttribute("nuevaAuditoriaForm", new NuevaAuditoriaForm());
        model.addAttribute("organizaciones", organizacionService.listarTodas());
        return "auditoria/nueva";
    }

    @PostMapping("/nueva")
    public String crear(@Valid @ModelAttribute("nuevaAuditoriaForm") NuevaAuditoriaForm form, BindingResult bindingResult,
                         @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("organizaciones", organizacionService.listarTodas());
            return "auditoria/nueva";
        }
        Organizacion organizacion = organizacionService.buscarPorId(form.getIdOrganizacion());
        Auditoria auditoria = auditoriaService.crear(organizacion, form.getFechaAuditoria(), usuarioPrincipal.getUsuario());
        return "redirect:/auditorias/" + auditoria.getIdAuditoria() + "/cuestionario";
    }

    @GetMapping("/{id}/cuestionario")
    public String cuestionario(@PathVariable Integer id, Model model) {
        Auditoria auditoria = auditoriaService.buscarPorId(id);
        List<GrupoControlCuestionario> grupos = auditoriaService.construirGrupos();
        CuestionarioForm form = auditoriaService.construirFormulario(id, grupos);

        model.addAttribute("auditoria", auditoria);
        model.addAttribute("grupos", grupos);
        model.addAttribute("cuestionarioForm", form);
        return "auditoria/cuestionario";
    }

    @PostMapping("/{id}/respuestas")
    public String guardarBorrador(@PathVariable Integer id, @ModelAttribute("cuestionarioForm") CuestionarioForm form,
                                   RedirectAttributes redirectAttributes) {
        auditoriaService.guardarRespuestas(id, form);
        redirectAttributes.addFlashAttribute("mensaje", "Borrador guardado correctamente.");
        return "redirect:/auditorias/" + id + "/cuestionario";
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Integer id, @ModelAttribute("cuestionarioForm") CuestionarioForm form,
                             RedirectAttributes redirectAttributes) {
        auditoriaService.guardarRespuestas(id, form);
        calculoAuditoriaService.finalizar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Auditoría finalizada. Resultados calculados.");
        return "redirect:/auditorias/" + id + "/resultados";
    }
}
