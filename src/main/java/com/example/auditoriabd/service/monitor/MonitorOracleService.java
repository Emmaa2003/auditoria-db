package com.example.auditoriabd.service.monitor;

import com.example.auditoriabd.dto.monitor.IndicadorComponenteView;
import com.example.auditoriabd.dto.monitor.MetricaDetalleView;
import com.example.auditoriabd.dto.monitor.MonitorOracleView;
import com.example.auditoriabd.entity.monitor.EstadoSalud;
import com.example.auditoriabd.entity.monitor.MonitorAlerta;
import com.example.auditoriabd.entity.monitor.MonitorIndice;
import com.example.auditoriabd.entity.monitor.NivelAlerta;
import com.example.auditoriabd.repository.monitor.MonitorAlertaRepository;
import com.example.auditoriabd.repository.monitor.MonitorIndiceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Orquestador del Monitor de Salud de Oracle: consulta las vistas dinamicas
 * via {@link OracleVistaDinamicaService}, calcula IP/IM/IA/ISBD y el "estado
 * real" con {@link IndicadorOracleUtil}, registra el historico y las alertas,
 * y arma la vista completa para la pantalla /monitor. El mismo metodo
 * {@link #medirYRegistrar()} lo llama tanto el job programado como el
 * controller en cada visita a la pagina, asi la pantalla siempre muestra
 * datos en vivo y a la vez alimenta el historico.
 */
@Service
public class MonitorOracleService {

    // Pesos internos de cada metrica cruda dentro de su sub-indice de salud (no configurables via
    // properties, a diferencia de los pesos IP/IM/IA del ISBD que si se calibran externamente).
    private static final BigDecimal PESO_PROC_UTIL = BigDecimal.valueOf(0.4);
    private static final BigDecimal PESO_SESS_UTIL = BigDecimal.valueOf(0.3);
    private static final BigDecimal PESO_BLOQUEADAS = BigDecimal.valueOf(0.3);

    private static final BigDecimal PESO_PGA_HIT = BigDecimal.valueOf(0.4);
    private static final BigDecimal PESO_OVERALLOC = BigDecimal.valueOf(0.3);
    private static final BigDecimal PESO_SGA = BigDecimal.valueOf(0.3);

    private static final BigDecimal PESO_DATAFILES = BigDecimal.valueOf(0.35);
    private static final BigDecimal PESO_TABLESPACES = BigDecimal.valueOf(0.35);
    private static final BigDecimal PESO_REDOLOGS = BigDecimal.valueOf(0.15);
    private static final BigDecimal PESO_TEMPFILES = BigDecimal.valueOf(0.15);

    private final OracleVistaDinamicaService vistaDinamicaService;
    private final MonitorIndiceRepository monitorIndiceRepository;
    private final MonitorAlertaRepository monitorAlertaRepository;

    private final BigDecimal pesoProcesos;
    private final BigDecimal pesoMemoria;
    private final BigDecimal pesoArchivos;

    public MonitorOracleService(OracleVistaDinamicaService vistaDinamicaService,
                                 MonitorIndiceRepository monitorIndiceRepository,
                                 MonitorAlertaRepository monitorAlertaRepository,
                                 @Value("${monitor.oracle.peso-procesos}") BigDecimal pesoProcesos,
                                 @Value("${monitor.oracle.peso-memoria}") BigDecimal pesoMemoria,
                                 @Value("${monitor.oracle.peso-archivos}") BigDecimal pesoArchivos) {
        this.vistaDinamicaService = vistaDinamicaService;
        this.monitorIndiceRepository = monitorIndiceRepository;
        this.monitorAlertaRepository = monitorAlertaRepository;
        this.pesoProcesos = pesoProcesos;
        this.pesoMemoria = pesoMemoria;
        this.pesoArchivos = pesoArchivos;
    }

    /** Una metrica cruda evaluada, previa a convertirse en DTO de detalle o en alerta persistida. */
    private static class Metrica {
        final String componente;
        final String variable;
        final String valorTexto;
        final String umbralTexto;
        final NivelAlerta nivel;
        final String descripcion;

        Metrica(String componente, String variable, String valorTexto, String umbralTexto,
                NivelAlerta nivel, String descripcion) {
            this.componente = componente;
            this.variable = variable;
            this.valorTexto = valorTexto;
            this.umbralTexto = umbralTexto;
            this.nivel = nivel;
            this.descripcion = descripcion;
        }

        MetricaDetalleView toDetalle() {
            return new MetricaDetalleView(variable, valorTexto,
                    IndicadorOracleUtil.textoNivelAlerta(nivel), IndicadorOracleUtil.cssNivelAlerta(nivel));
        }

        MonitorAlerta toAlertaEntity() {
            MonitorAlerta alerta = new MonitorAlerta();
            alerta.setComponente(componente);
            alerta.setVariable(variable);
            alerta.setValor(valorTexto);
            alerta.setUmbral(umbralTexto);
            alerta.setNivel(nivel);
            alerta.setDescripcion(descripcion);
            return alerta;
        }
    }

    private static class ResultadoComponente {
        final BigDecimal salud;
        final List<Metrica> metricas;

        ResultadoComponente(BigDecimal salud, List<Metrica> metricas) {
            this.salud = salud;
            this.metricas = metricas;
        }
    }

    @Transactional
    public MonitorOracleView medirYRegistrar() {
        ResultadoComponente procesos = calcularProcesos();
        ResultadoComponente memoria = calcularMemoria();
        ResultadoComponente archivos = calcularArchivos();

        BigDecimal isbd = IndicadorOracleUtil.clamp(
                pesoProcesos.multiply(procesos.salud)
                        .add(pesoMemoria.multiply(memoria.salud))
                        .add(pesoArchivos.multiply(archivos.salud)));

        boolean hayAlertaCritica = todas(procesos, memoria, archivos).stream()
                .anyMatch(m -> m.nivel == NivelAlerta.CRITICO);

        EstadoSalud estadoIsbd = IndicadorOracleUtil.estadoIsbd(isbd);
        EstadoSalud estadoReal = IndicadorOracleUtil.aplicarPisoCritico(
                estadoIsbd, hayAlertaCritica, procesos.salud, memoria.salud, archivos.salud);

        List<String> causas = construirCausas(procesos, memoria, archivos, estadoReal);

        // Persistir el punto historico (se guarda el estado REAL, no el declarado, para que el
        // historico nunca muestre "saludable" cuando en realidad hubo un problema critico oculto).
        MonitorIndice indice = new MonitorIndice();
        indice.setIndicadorProcesos(procesos.salud);
        indice.setIndicadorMemoria(memoria.salud);
        indice.setIndicadorArchivos(archivos.salud);
        indice.setIndiceSalud(isbd);
        indice.setEstado(estadoReal);
        monitorIndiceRepository.save(indice);

        // Persistir y coleccionar las alertas de esta medicion (solo las que no estan en NORMAL).
        List<MonitorAlerta> alertasGuardadas = new ArrayList<>();
        for (Metrica metrica : todas(procesos, memoria, archivos)) {
            if (metrica.nivel != NivelAlerta.NORMAL) {
                alertasGuardadas.add(monitorAlertaRepository.save(metrica.toAlertaEntity()));
            }
        }

        MonitorOracleView view = new MonitorOracleView();
        view.setIsbd(isbd);
        view.setEstadoIsbd(estadoIsbd);
        view.setEstadoReal(estadoReal);
        view.setCausasEstadoReal(causas);
        view.setIp(aComponenteView("Procesos", procesos));
        view.setIm(aComponenteView("Memoria", memoria));
        view.setIa(aComponenteView("Archivos", archivos));
        view.setAlertasActuales(alertasGuardadas);
        view.setHistorico(historial(30));
        return view;
    }

    /** Los ultimos N puntos, en orden CRONOLOGICO (mas antiguo primero) para graficar la evolucion. */
    public List<MonitorIndice> historial(int ultimosN) {
        List<MonitorIndice> recientes = monitorIndiceRepository.findAllByOrderByFechaHoraDesc(PageRequest.of(0, ultimosN));
        Collections.reverse(recientes);
        return recientes;
    }

    private List<Metrica> todas(ResultadoComponente... resultados) {
        List<Metrica> todas = new ArrayList<>();
        for (ResultadoComponente r : resultados) {
            todas.addAll(r.metricas);
        }
        return todas;
    }

    private IndicadorComponenteView aComponenteView(String nombre, ResultadoComponente resultado) {
        List<MetricaDetalleView> detalle = new ArrayList<>();
        for (Metrica metrica : resultado.metricas) {
            detalle.add(metrica.toDetalle());
        }
        EstadoSalud estadoSubindice = IndicadorOracleUtil.estadoIsbd(resultado.salud);
        return new IndicadorComponenteView(nombre, resultado.salud,
                IndicadorOracleUtil.textoEstadoSalud(estadoSubindice),
                IndicadorOracleUtil.cssEstadoSalud(estadoSubindice), detalle);
    }

    private List<String> construirCausas(ResultadoComponente procesos, ResultadoComponente memoria,
                                          ResultadoComponente archivos, EstadoSalud estadoReal) {
        if (estadoReal != EstadoSalud.CRITICO) {
            return List.of();
        }
        List<String> causas = new ArrayList<>();
        agregarSiCritico(causas, "procesos", procesos.salud);
        agregarSiCritico(causas, "memoria", memoria.salud);
        agregarSiCritico(causas, "archivos", archivos.salud);
        for (Metrica metrica : todas(procesos, memoria, archivos)) {
            if (metrica.nivel == NivelAlerta.CRITICO) {
                causas.add(metrica.descripcion);
            }
        }
        return causas;
    }

    private void agregarSiCritico(List<String> causas, String nombre, BigDecimal salud) {
        if (salud.compareTo(IndicadorOracleUtil.PISO_CRITICO_SUBINDICE) < 0) {
            causas.add("Indicador de " + nombre + " en nivel crítico (" + salud + ")");
        }
    }

    // ---- Indicador de Procesos (IP) ----

    private ResultadoComponente calcularProcesos() {
        Map<String, OracleVistaDinamicaService.LimiteRecurso> limites = vistaDinamicaService.obtenerLimitesRecursos();
        BigDecimal procUtil = pct(limites.get("processes"));
        BigDecimal sessUtil = pct(limites.get("sessions"));
        long bloqueadas = vistaDinamicaService.contarSesionesBloqueadas();

        List<Metrica> metricas = new ArrayList<>();
        metricas.add(new Metrica("PROCESOS", "Uso de procesos", texto(procUtil), "85% / 95%",
                IndicadorOracleUtil.claseUtilizacion(procUtil.doubleValue()),
                "Uso de procesos respecto al límite configurado: " + texto(procUtil) + "."));
        metricas.add(new Metrica("PROCESOS", "Uso de sesiones", texto(sessUtil), "85% / 95%",
                IndicadorOracleUtil.claseUtilizacion(sessUtil.doubleValue()),
                "Uso de sesiones respecto al límite configurado: " + texto(sessUtil) + "."));
        metricas.add(new Metrica("PROCESOS", "Sesiones bloqueadas", bloqueadas + " sesión(es)", "0",
                bloqueadas > 0 ? NivelAlerta.CRITICO : NivelAlerta.NORMAL,
                bloqueadas > 0
                        ? "Existen " + bloqueadas + " sesión(es) bloqueada(s) esperando por otra sesión."
                        : "No hay sesiones bloqueadas."));

        BigDecimal scoreProc = IndicadorOracleUtil.invertirUtilizacion(procUtil);
        BigDecimal scoreSess = IndicadorOracleUtil.invertirUtilizacion(sessUtil);
        BigDecimal scoreBloqueadas = bloqueadas == 0
                ? BigDecimal.valueOf(100)
                : IndicadorOracleUtil.clamp(BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(bloqueadas * 20)));

        BigDecimal ip = IndicadorOracleUtil.clamp(
                PESO_PROC_UTIL.multiply(scoreProc)
                        .add(PESO_SESS_UTIL.multiply(scoreSess))
                        .add(PESO_BLOQUEADAS.multiply(scoreBloqueadas)));

        return new ResultadoComponente(ip, metricas);
    }

    private BigDecimal pct(OracleVistaDinamicaService.LimiteRecurso limite) {
        return limite == null ? BigDecimal.ZERO : limite.getPorcentajeUtilizacion();
    }

    // ---- Indicador de Memoria (IM) ----

    private ResultadoComponente calcularMemoria() {
        Map<String, BigDecimal> pga = vistaDinamicaService.obtenerEstadisticasPga();
        Map<String, BigDecimal> sga = vistaDinamicaService.obtenerInfoSga();

        BigDecimal pgaHit = pga.getOrDefault("cache hit percentage", BigDecimal.ZERO);
        long overAlloc = pga.getOrDefault("over allocation count", BigDecimal.ZERO).longValue();

        BigDecimal totalSga = sga.getOrDefault("Maximum SGA Size", BigDecimal.ZERO);
        BigDecimal freeSga = sga.getOrDefault("Free SGA Memory Available", BigDecimal.ZERO);
        BigDecimal sgaUsedPct = totalSga.compareTo(BigDecimal.ZERO) > 0
                ? totalSga.subtract(freeSga).divide(totalSga, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<Metrica> metricas = new ArrayList<>();
        // El "fallo" de cache (100 - hit%) es la version "mas alto = peor" de esta metrica, para
        // poder clasificarla con la misma escala NORMAL/ADVERTENCIA/ALTO/CRITICO que el resto.
        BigDecimal fallosCache = IndicadorOracleUtil.clamp(BigDecimal.valueOf(100).subtract(pgaHit));
        metricas.add(new Metrica("MEMORIA", "Cache hit de PGA", texto(pgaHit) + " aciertos", "< 70% aciertos",
                IndicadorOracleUtil.claseUtilizacion(fallosCache.doubleValue()),
                "Porcentaje de aciertos de la cache de PGA: " + texto(pgaHit) + "."));
        metricas.add(new Metrica("MEMORIA", "Over-allocation de PGA", overAlloc + " evento(s)", "0",
                claseConteo(overAlloc, 3, 10),
                overAlloc > 0
                        ? "La PGA ha excedido PGA_AGGREGATE_TARGET " + overAlloc + " vez/veces."
                        : "Sin eventos de over-allocation de PGA."));
        // Uso de "Free SGA Memory Available" no se clasifica con la escala generica de utilizacion:
        // llegar a 0% libre (100% "usado" contra el techo SGA_MAX_SIZE) es el estado ESPERADO en
        // regimen estable de Automatic Shared Memory Management, no una senal de agotamiento como
        // si fuera uso de procesos/sesiones/tablespace - comprobado contra la base real, donde este
        // valor da 100% de forma consistente sin que la instancia tenga ningun problema. Por eso usa
        // una clasificacion propia, mucho mas tolerante, en vez de la escala 70/85/95 generica.
        metricas.add(new Metrica("MEMORIA", "Uso de SGA", texto(sgaUsedPct), "90% (solo penaliza salud sobre este umbral)",
                IndicadorOracleUtil.claseUsoSga(sgaUsedPct.doubleValue()),
                "Uso de la SGA respecto al techo SGA_MAX_SIZE: " + texto(sgaUsedPct) + "."));

        BigDecimal scoreOveralloc = overAlloc == 0
                ? BigDecimal.valueOf(100)
                : IndicadorOracleUtil.clamp(BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(overAlloc * 25)));
        BigDecimal umbralSga = BigDecimal.valueOf(90);
        BigDecimal scoreSga = sgaUsedPct.compareTo(umbralSga) <= 0
                ? BigDecimal.valueOf(100)
                : IndicadorOracleUtil.clamp(BigDecimal.valueOf(100)
                        .subtract(sgaUsedPct.subtract(umbralSga).multiply(BigDecimal.valueOf(5))));

        BigDecimal im = IndicadorOracleUtil.clamp(
                PESO_PGA_HIT.multiply(pgaHit)
                        .add(PESO_OVERALLOC.multiply(scoreOveralloc))
                        .add(PESO_SGA.multiply(scoreSga)));

        return new ResultadoComponente(im, metricas);
    }

    // ---- Indicador de Archivos (IA) ----

    private ResultadoComponente calcularArchivos() {
        Map<String, Long> datafiles = vistaDinamicaService.obtenerEstadoDatafiles();
        Map<String, Long> tempfiles = vistaDinamicaService.obtenerEstadoTempfiles();
        Map<String, Long> redologs = vistaDinamicaService.obtenerEstadoRedoLogs();
        List<OracleVistaDinamicaService.UsoTablespace> tablespaces = vistaDinamicaService.obtenerUsoTablespaces();

        long totalDf = datafiles.values().stream().mapToLong(Long::longValue).sum();
        // El datafile del tablespace SYSTEM siempre reporta status='SYSTEM' (nunca 'ONLINE') aunque
        // este perfectamente sano - comprobado contra V$DATAFILE real. Contarlo como "fuera de linea"
        // generaba una alerta CRITICA falsa en toda instalacion Oracle (bug real encontrado y corregido).
        long onlineDf = datafiles.getOrDefault("ONLINE", 0L) + datafiles.getOrDefault("SYSTEM", 0L);
        long offlineDf = totalDf - onlineDf;

        long totalTf = tempfiles.values().stream().mapToLong(Long::longValue).sum();
        long onlineTf = tempfiles.getOrDefault("ONLINE", 0L);
        long offlineTf = totalTf - onlineTf;

        long redologsInvalidos = redologs.getOrDefault("INVALID", 0L) + redologs.getOrDefault("DELETED", 0L);

        BigDecimal peorTablespace = tablespaces.stream()
                .map(OracleVistaDinamicaService.UsoTablespace::getPorcentajeUsado)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        List<Metrica> metricas = new ArrayList<>();
        metricas.add(new Metrica("ARCHIVOS", "Datafiles en línea", onlineDf + "/" + totalDf, "= total",
                offlineDf > 0 ? NivelAlerta.CRITICO : NivelAlerta.NORMAL,
                offlineDf > 0 ? "Hay " + offlineDf + " datafile(s) fuera de línea." : "Todos los datafiles están en línea."));
        metricas.add(new Metrica("ARCHIVOS", "Tempfiles en línea", onlineTf + "/" + totalTf, "= total",
                offlineTf > 0 ? NivelAlerta.CRITICO : NivelAlerta.NORMAL,
                offlineTf > 0 ? "Hay " + offlineTf + " tempfile(s) fuera de línea." : "Todos los tempfiles están en línea."));
        metricas.add(new Metrica("ARCHIVOS", "Redo logs con problemas", redologsInvalidos + " miembro(s)", "0",
                redologsInvalidos > 0 ? NivelAlerta.CRITICO : NivelAlerta.NORMAL,
                redologsInvalidos > 0
                        ? "Hay " + redologsInvalidos + " miembro(s) de redo log inválido(s)/eliminado(s)."
                        : "Todos los miembros de redo log están sanos."));
        for (OracleVistaDinamicaService.UsoTablespace ts : tablespaces) {
            BigDecimal pctUsado = ts.getPorcentajeUsado();
            NivelAlerta nivel = IndicadorOracleUtil.claseUtilizacion(pctUsado.doubleValue());
            metricas.add(new Metrica("ARCHIVOS", "Tablespace " + ts.getNombre(), texto(pctUsado), "85% / 95%", nivel,
                    "Uso del tablespace " + ts.getNombre() + ": " + texto(pctUsado) + "."));
        }

        BigDecimal scoreDatafiles = totalDf == 0 ? BigDecimal.valueOf(100)
                : porcentaje(onlineDf, totalDf, offlineDf > 0);
        BigDecimal scoreTempfiles = totalTf == 0 ? BigDecimal.valueOf(100)
                : porcentaje(onlineTf, totalTf, offlineTf > 0);
        BigDecimal umbralTablespace = BigDecimal.valueOf(85);
        BigDecimal scoreTablespaces = peorTablespace.compareTo(umbralTablespace) <= 0
                ? BigDecimal.valueOf(100)
                : IndicadorOracleUtil.clamp(BigDecimal.valueOf(100)
                        .subtract(peorTablespace.subtract(umbralTablespace).multiply(BigDecimal.valueOf(3))));
        BigDecimal scoreRedologs = redologsInvalidos == 0
                ? BigDecimal.valueOf(100)
                : IndicadorOracleUtil.clamp(BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(redologsInvalidos * 30)));

        BigDecimal ia = IndicadorOracleUtil.clamp(
                PESO_DATAFILES.multiply(scoreDatafiles)
                        .add(PESO_TABLESPACES.multiply(scoreTablespaces))
                        .add(PESO_REDOLOGS.multiply(scoreRedologs))
                        .add(PESO_TEMPFILES.multiply(scoreTempfiles)));

        return new ResultadoComponente(ia, metricas);
    }

    /** % en línea, con tope duro de 50 si hay al menos un archivo fuera de línea (senal dura). */
    private BigDecimal porcentaje(long parte, long total, boolean hayFueraDeLinea) {
        BigDecimal pct = BigDecimal.valueOf(parte)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (hayFueraDeLinea) {
            pct = pct.min(BigDecimal.valueOf(50));
        }
        return IndicadorOracleUtil.clamp(pct);
    }

    private NivelAlerta claseConteo(long conteo, long umbralAdvertencia, long umbralCritico) {
        if (conteo >= umbralCritico) return NivelAlerta.CRITICO;
        if (conteo >= umbralAdvertencia) return NivelAlerta.ALTO;
        if (conteo > 0) return NivelAlerta.ADVERTENCIA;
        return NivelAlerta.NORMAL;
    }

    private String texto(BigDecimal porcentaje) {
        return porcentaje.setScale(2, RoundingMode.HALF_UP) + "%";
    }
}
