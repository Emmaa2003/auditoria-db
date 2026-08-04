package com.example.auditoriabd.config;

import com.example.auditoriabd.entity.Control;
import com.example.auditoriabd.entity.PesoControl;
import com.example.auditoriabd.entity.Pregunta;
import com.example.auditoriabd.entity.RolUsuario;
import com.example.auditoriabd.entity.Usuario;
import com.example.auditoriabd.repository.ControlRepository;
import com.example.auditoriabd.repository.PreguntaRepository;
import com.example.auditoriabd.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ControlRepository controlRepository;
    private final PreguntaRepository preguntaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(ControlRepository controlRepository, PreguntaRepository preguntaRepository,
                       UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.controlRepository = controlRepository;
        this.preguntaRepository = preguntaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (controlRepository.count() == 0) {
            seedControlesYPreguntas();
        }
        if (usuarioRepository.findByCorreo("admin@auditoria.com").isEmpty()) {
            seedAdmin();
        }
    }

    private void seedAdmin() {
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setCorreo("admin@auditoria.com");
        admin.setContrasenaHash(passwordEncoder.encode("admin123"));
        admin.setRol(RolUsuario.ADMINISTRADOR);
        admin.setActivo(true);
        usuarioRepository.save(admin);
    }

    private void seedControlesYPreguntas() {
        crearControl("A.8.2", "Acceso privilegiado", "Tecnológico", PesoControl.ALTO, true, true, false,
                new String[]{
                        "¿Existe una lista documentada de los usuarios con acceso de administrador a la base de datos?",
                        "¿Solo el DBA y personal autorizado tienen permisos de administrador?",
                        "¿Se revisa periódicamente quién tiene acceso privilegiado?",
                        "¿Se revocan los accesos de administrador cuando ya no son necesarios?"
                });

        crearControl("A.8.3", "Restricción de acceso a la información", "Tecnológico", PesoControl.ALTO, true, false, false,
                new String[]{
                        "¿Existen roles de usuario definidos según la función de cada persona?",
                        "¿Cada usuario accede únicamente a los datos que necesita para su trabajo?",
                        "¿Se revisa periódicamente que los permisos asignados sigan siendo correctos?"
                });

        crearControl("A.8.5", "Autenticación segura", "Tecnológico", PesoControl.ALTO, true, true, false,
                new String[]{
                        "¿Se requiere una contraseña para acceder a la base de datos?",
                        "¿Existen reglas de complejidad para las contraseñas (longitud, caracteres especiales, etc.)?",
                        "¿Existe autenticación de dos factores para accesos críticos?",
                        "¿Las contraseñas se cambian periódicamente?"
                });

        crearControl("A.8.13", "Copia de seguridad", "Tecnológico", PesoControl.ALTO, false, false, true,
                new String[]{
                        "¿Se realizan copias de seguridad periódicas de la base de datos?",
                        "¿Existe un calendario documentado de respaldos?",
                        "¿Se ha probado restaurar una copia de seguridad en el último año?",
                        "¿Se supervisa que los respaldos se ejecuten correctamente?"
                });

        crearControl("A.8.15", "Registro y monitoreo de eventos", "Tecnológico", PesoControl.MEDIO, true, true, false,
                new String[]{
                        "¿El sistema registra quién accede a la base de datos y qué acciones realiza?",
                        "¿Los registros (logs) se revisan periódicamente?",
                        "¿Existen alertas automáticas ante actividad sospechosa?"
                });

        crearControl("A.8.24", "Uso de criptografía", "Tecnológico", PesoControl.ALTO, true, false, false,
                new String[]{
                        "¿Los datos sensibles (contraseñas, datos personales, financieros) están encriptados?",
                        "¿La información se protege también cuando viaja entre el sistema y la base de datos?",
                        "¿Existe una política definida sobre qué datos deben encriptarse?"
                });

        crearControl("A.8.8", "Gestión de vulnerabilidades técnicas", "Tecnológico", PesoControl.MEDIO, true, true, true,
                new String[]{
                        "¿El software de la base de datos se mantiene actualizado con los últimos parches?",
                        "¿Se revisan periódicamente vulnerabilidades conocidas del sistema?",
                        "¿Existe un procedimiento documentado para aplicar actualizaciones de seguridad?"
                });

        crearControl("A.5.15", "Control de acceso (política)", "Organizacional", PesoControl.MEDIO, true, false, false,
                new String[]{
                        "¿Existe un procedimiento documentado para solicitar acceso a la base de datos?",
                        "¿Existe un proceso formal de aprobación antes de otorgar un acceso?",
                        "¿Se revocan los accesos cuando una persona cambia de puesto o deja la organización?"
                });

        crearControl("A.6.2", "Segregación de funciones", "De personas", PesoControl.MEDIO, false, true, false,
                new String[]{
                        "¿La persona que solicita un acceso es diferente de quien lo aprueba?",
                        "¿La persona que aprueba un acceso es diferente de quien lo otorga técnicamente?",
                        "¿El trabajo del DBA es supervisado por alguien más?"
                });

        crearControl("A.7.4", "Seguridad física", "Físico", PesoControl.MEDIO, false, false, true,
                new String[]{
                        "¿El servidor de la base de datos está en un lugar de acceso restringido (llave, tarjeta, etc.)?",
                        "¿Existen cámaras de seguridad u otro control de vigilancia en esa ubicación?",
                        "¿Existen medidas contra riesgos físicos (incendio, inundación, corte de energía)?"
                });
    }

    private void crearControl(String codigo, String nombre, String dominio, PesoControl peso,
                               boolean afectaC, boolean afectaI, boolean afectaD, String[] preguntas) {
        Control control = new Control();
        control.setCodigo(codigo);
        control.setNombre(nombre);
        control.setDominio(dominio);
        control.setPeso(peso);
        control.setAfectaC(afectaC);
        control.setAfectaI(afectaI);
        control.setAfectaD(afectaD);
        control = controlRepository.save(control);

        List<Pregunta> lista = crearPreguntas(control, preguntas);
        preguntaRepository.saveAll(lista);
    }

    private List<Pregunta> crearPreguntas(Control control, String[] textos) {
        List<Pregunta> preguntas = Arrays.asList(new Pregunta[textos.length]);
        for (int i = 0; i < textos.length; i++) {
            Pregunta pregunta = new Pregunta();
            pregunta.setControl(control);
            pregunta.setTexto(textos[i]);
            pregunta.setOrden(i + 1);
            preguntas.set(i, pregunta);
        }
        return preguntas;
    }
}
