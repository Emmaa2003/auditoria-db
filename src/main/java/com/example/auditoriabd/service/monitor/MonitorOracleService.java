package com.example.auditoriabd.service.monitor;

import com.example.auditoriabd.dto.monitor.GrupoAlertasView;
import com.example.auditoriabd.dto.monitor.HistoricoTablespaceView;
import com.example.auditoriabd.dto.monitor.IndicadorComponenteView;
import com.example.auditoriabd.dto.monitor.MetricaDetalleView;
import com.example.auditoriabd.dto.monitor.MonitorOracleView;
import com.example.auditoriabd.entity.monitor.EstadoSalud;
import com.example.auditoriabd.entity.monitor.MonitorAlerta;
import com.example.auditoriabd.entity.monitor.MonitorArchivos;
import com.example.auditoriabd.entity.monitor.MonitorIndice;
import com.example.auditoriabd.entity.monitor.MonitorMemoria;
import com.example.auditoriabd.entity.monitor.MonitorProcesos;
import com.example.auditoriabd.entity.monitor.MonitorTablespace;
import com.example.auditoriabd.entity.monitor.NivelAlerta;
import com.example.auditoriabd.repository.monitor.MonitorAlertaRepository;
import com.example.auditoriabd.repository.monitor.MonitorArchivosRepository;
import com.example.auditoriabd.repository.monitor.MonitorIndiceRepository;
import com.example.auditoriabd.repository.monitor.MonitorMemoriaRepository;
import com.example.auditoriabd.repository.monitor.MonitorProcesosRepository;
import com.example.auditoriabd.repository.monitor.MonitorTablespaceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Orquestador del Monitor de Salud de Oracle: consulta las vistas dinamicas
 * via {@link OracleVistaDinamicaService}, calcula IP/IM/IA/ISBD y el "estado
 * real" con {@link IndicadorOracleUtil}, registra el historico (indice
 * combinado, por componente y por tablespace) y las alertas (correlacionadas
 * por componente y por persistencia entre ciclos), y arma la vista completa
 * para la pantalla /monitor. El mismo metodo {@link #medirYRegistrar()} lo
 * llama tanto el job programado como el controller en cada visita a la
 * pagina, asi la pantalla siempre muestra datos en vivo y a la vez alimenta
 * el historico.
 */
@Service
public class MonitorOracleService {

    // Pesos internos de cada metrica cruda dentro de su sub-indice de salud (no configurables via
    // properties, a diferencia de los pesos IP/IM/IA del ISBD que si se calibran externamente).
    private static final BigDecimal PESO_PROC_UTIL = BigDecimal.valueOf(0.3);
    private static final BigDecimal PESO_SESS_UTIL = BigDecimal.valueOf(0.25);
    private static final BigDecimal PESO_BLOQUEADAS = BigDecimal.valueOf(0.3);
    private static final BigDecimal PESO_LONGOPS = BigDecimal.valueOf(0.15);

    private static final BigDecimal PESO_PGA_HIT = BigDecimal.valueOf(0.4);
    private static final BigDecimal PESO_OVERALLOC = BigDecimal.valueOf(0.3);
    private static final BigDecimal PESO_SGA = BigDecimal.valueOf(0.3);

    private static final BigDecimal PESO_DATAFILES = BigDecimal.valueOf(0.35);
    private static final BigDecimal PESO_TABLESPACES = BigDecimal.valueOf(0.35);
    private static final BigDecimal PESO_REDOLOGS = BigDecimal.valueOf(0.15);
    private static final BigDecimal PESO_TEMPFILES = BigDecimal.valueOf(0.15);

    /**
     * Ventana dentro de la cual dos alertas identicas (mismo componente+variable+nivel) en ciclos
     * consecutivos se consideran LA MISMA condicion persistente (se actualiza la fila existente en
     * vez de insertar un duplicado) - "relacionar las alertas" de la guia general. Generosa a
     * proposito para cubrir tanto el ciclo del scheduler como visitas manuales a la pagina.
     */
    private static final Duration VENTANA_PERSISTENCIA = Duration.ofMinutes(5);

    private final OracleVistaDinamicaService vistaDinamicaService;
    private final MonitorIndiceRepository monitorIndiceRepository;
    private final MonitorAlertaRepository monitorAlertaRepository;
    private final MonitorProcesosRepository monitorProcesosRepository;
    private final MonitorMemoriaRepository monitorMemoriaRepository;
    private final MonitorArchivosRepository monitorArchivosRepository;
    private final MonitorTablespaceRepository monitorTablespaceRepository;

    private final BigDecimal pesoProcesos;
    private final BigDecimal pesoMemoria;
    private final BigDecimal pesoArchivos;

    public MonitorOracleService(OracleVistaDinamicaService vistaDinamicaService,
                                 MonitorIndiceRepository monitorIndiceRepository,
                                 MonitorAlertaRepository monitorAlertaRepository,
                                 MonitorProcesosRepository monitorProcesosRepository,
                                 MonitorMemoriaRepository monitorMemoriaRepository,
                                 MonitorArchivosRepository monitorArchivosRepository,
                                 MonitorTablespaceRepository monitorTablespaceRepository,
                                 @Value("${monitor.oracle.peso-procesos}") BigDecimal pesoProcesos,
                                 @Value("${monitor.oracle.peso-memoria}") BigDecimal pesoMemoria,
                                 @Value("${monitor.oracle.peso-archivos}") BigDecimal pesoArchivos) {
        this.vistaDinamicaService = vistaDinamicaService;
        this.monitorIndiceRepository = monitorIndiceRepository;
        this.monitorAlertaRepository = monitorAlertaRepository;
        this.monitorProcesosRepository = monitorProcesosRepository;
        this.monitorMemoriaRepository = monitorMemoriaRepository;
        this.monitorArchivosRepository = monitorArchivosRepository;
        this.monitorTablespaceRepository = monitorTablespaceRepository;
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

    private static class ResultadoProcesos {
        final BigDecimal salud;
        final List<Metrica> metricas;
        final BigDecimal usoProcesosPct;
        final BigDecimal usoSesionesPct;
        final long sesionesBloqueadas;
        final long operacionesProlongadas;

        ResultadoProcesos(BigDecimal salud, List<Metrica> metricas, BigDecimal usoProcesosPct,
                           BigDecimal usoSesionesPct, long sesionesBloqueadas, long operacionesProlongadas) {
            this.salud = salud;
            this.metricas = metricas;
            this.usoProcesosPct = usoProcesosPct;
            this.usoSesionesPct = usoSesionesPct;
            this.sesionesBloqueadas = sesionesBloqueadas;
            this.operacionesProlongadas = operacionesProlongadas;
        }
    }

    private static class ResultadoMemoria {
        final BigDecimal salud;
        final List<Metrica> metricas;
        final BigDecimal cacheHitPgaPct;
        final long overAllocationPga;
        final BigDecimal usoSgaPct;

        ResultadoMemoria(BigDecimal salud, List<Metrica> metricas, BigDecimal cacheHitPgaPct,
                          long overAllocationPga, BigDecimal usoSgaPct) {
            this.salud = salud;
            this.metricas = metricas;
            this.cacheHitPgaPct = cacheHitPgaPct;
            this.overAllocationPga = overAllocationPga;
            this.usoSgaPct = usoSgaPct;
        }
    }

    private static class ResultadoArchivos {
        final BigDecimal salud;
        final List<Metrica> metricas;
        final long datafilesOnline;
        final long datafilesTotal;
        final long tempfilesOnline;
        final long tempfilesTotal;
        final long redologsInvalidos;
        final List<OracleVistaDinamicaService.UsoTablespace> tablespaces;

        ResultadoArchivos(BigDecimal salud, List<Metrica> metricas, long datafilesOnline, long datafilesTotal,
                           long tempfilesOnline, long tempfilesTotal, long redologsInvalidos,
                           List<OracleVistaDinamicaService.UsoTablespace> tablespaces) {
            this.salud = salud;
            this.metricas = metricas;
            this.datafilesOnline = datafilesOnline;
            this.datafilesTotal = datafilesTotal;
            this.tempfilesOnline = tempfilesOnline;
            this.tempfilesTotal = tempfilesTotal;
            this.redologsInvalidos = redologsInvalidos;
            this.tablespaces = tablespaces;
        }
    }

    @Transactional
    public MonitorOracleView medirYRegistrar() {
        ResultadoProcesos procesos = calcularProcesos();
        ResultadoMemoria memoria = calcularMemoria();
        ResultadoArchivos archivos = calcularArchivos();

        BigDecimal isbd = IndicadorOracleUtil.clamp(
                pesoProcesos.multiply(procesos.salud)
                        .add(pesoMemoria.multiply(memoria.salud))
                        .add(pesoArchivos.multiply(archivos.salud)));

        List<Metrica> todasMetricas = new ArrayList<>();
        todasMetricas.addAll(procesos.metricas);
        todasMetricas.addAll(memoria.metricas);
        todasMetricas.addAll(archivos.metricas);

        boolean hayAlertaCritica = todasMetricas.stream().anyMatch(m -> m.nivel == NivelAlerta.CRITICO);

        EstadoSalud estadoIsbd = IndicadorOracleUtil.estadoIsbd(isbd);
        EstadoSalud estadoReal = IndicadorOracleUtil.aplicarPisoCritico(
                estadoIsbd, hayAlertaCritica, procesos.salud, memoria.salud, archivos.salud);

        List<String> causas = construirCausas(todasMetricas, procesos.salud, memoria.salud, archivos.salud, estadoReal);

        // ---- Persistir el punto historico combinado (se guarda el estado REAL) ----
        MonitorIndice indice = new MonitorIndice();
        indice.setIndicadorProcesos(procesos.salud);
        indice.setIndicadorMemoria(memoria.salud);
        indice.setIndicadorArchivos(archivos.salud);
        indice.setIndiceSalud(isbd);
        indice.setEstado(estadoReal);
        monitorIndiceRepository.save(indice);

        // ---- Persistir el snapshot crudo por componente (resuelve "¿está empeorando?" por variable) ----
        MonitorProcesos snapshotProcesos = new MonitorProcesos();
        snapshotProcesos.setIndicadorProcesos(procesos.salud);
        snapshotProcesos.setUsoProcesosPct(procesos.usoProcesosPct);
        snapshotProcesos.setUsoSesionesPct(procesos.usoSesionesPct);
        snapshotProcesos.setSesionesBloqueadas(procesos.sesionesBloqueadas);
        snapshotProcesos.setOperacionesProlongadas(procesos.operacionesProlongadas);
        monitorProcesosRepository.save(snapshotProcesos);

        MonitorMemoria snapshotMemoria = new MonitorMemoria();
        snapshotMemoria.setIndicadorMemoria(memoria.salud);
        snapshotMemoria.setCacheHitPgaPct(memoria.cacheHitPgaPct);
        snapshotMemoria.setOverAllocationPga(memoria.overAllocationPga);
        snapshotMemoria.setUsoSgaPct(memoria.usoSgaPct);
        monitorMemoriaRepository.save(snapshotMemoria);

        MonitorArchivos snapshotArchivos = new MonitorArchivos();
        snapshotArchivos.setIndicadorArchivos(archivos.salud);
        snapshotArchivos.setDatafilesOnline(archivos.datafilesOnline);
        snapshotArchivos.setDatafilesTotal(archivos.datafilesTotal);
        snapshotArchivos.setTempfilesOnline(archivos.tempfilesOnline);
        snapshotArchivos.setTempfilesTotal(archivos.tempfilesTotal);
        snapshotArchivos.setRedologsInvalidos(archivos.redologsInvalidos);
        monitorArchivosRepository.save(snapshotArchivos);

        for (OracleVistaDinamicaService.UsoTablespace ts : archivos.tablespaces) {
            MonitorTablespace snapshotTs = new MonitorTablespace();
            snapshotTs.setTablespaceName(ts.getNombre());
            snapshotTs.setPorcentajeUsado(ts.getPorcentajeUsado());
            monitorTablespaceRepository.save(snapshotTs);
        }

        // ---- Alertas: persistir o actualizar (condiciones persistentes no duplican fila) ----
        List<MonitorAlerta> alertasGuardadas = new ArrayList<>();
        for (Metrica metrica : todasMetricas) {
            if (metrica.nivel != NivelAlerta.NORMAL) {
                alertasGuardadas.add(persistirOActualizarAlerta(metrica));
            }
        }

        MonitorOracleView view = new MonitorOracleView();
        view.setIsbd(isbd);
        view.setEstadoIsbd(estadoIsbd);
        view.setEstadoReal(estadoReal);
        view.setCausasEstadoReal(causas);
        view.setIp(aComponenteView("Procesos", procesos.salud, procesos.metricas));
        view.setIm(aComponenteView("Memoria", memoria.salud, memoria.metricas));
        view.setIa(aComponenteView("Archivos", archivos.salud, archivos.metricas));
        view.setAlertasActuales(alertasGuardadas);
        view.setGruposAlertas(agruparAlertas(alertasGuardadas));
        view.setHistorico(historial(30));
        view.setHistoricoProcesos(historialProcesos(30));
        view.setHistoricoMemoria(historialMemoria(30));
        view.setHistoricoArchivos(historialArchivos(30));
        view.setHistoricoTablespaces(historialTablespaces());
        return view;
    }

    /**
     * "Relacionar las alertas" (2/2): si la MISMA condicion (componente+variable) sigue en el
     * MISMO nivel dentro de {@link #VENTANA_PERSISTENCIA}, se trata como la continuacion de un
     * unico incidente (se actualiza fecha_hora/valor/ocurrencias sobre la fila existente) en vez
     * de insertar una fila identica nueva en cada ciclo. Si el nivel cambio (escalo o bajo) o paso
     * demasiado tiempo desde la ultima vez, se registra como un incidente nuevo.
     */
    private MonitorAlerta persistirOActualizarAlerta(Metrica metrica) {
        LocalDateTime ahora = LocalDateTime.now();
        Optional<MonitorAlerta> ultima = monitorAlertaRepository
                .findTopByComponenteAndVariableOrderByFechaHoraDesc(metrica.componente, metrica.variable);

        if (ultima.isPresent()) {
            MonitorAlerta previa = ultima.get();
            boolean mismoNivel = previa.getNivel() == metrica.nivel;
            boolean dentroDeVentana = previa.getFechaHora().isAfter(ahora.minus(VENTANA_PERSISTENCIA));
            if (mismoNivel && dentroDeVentana) {
                previa.setFechaHora(ahora);
                previa.setValor(metrica.valorTexto);
                previa.setUmbral(metrica.umbralTexto);
                previa.setDescripcion(metrica.descripcion);
                previa.setOcurrencias(previa.getOcurrencias() + 1);
                return monitorAlertaRepository.save(previa);
            }
        }
        MonitorAlerta nueva = metrica.toAlertaEntity();
        nueva.setFechaHora(ahora);
        nueva.setFechaPrimera(ahora);
        nueva.setOcurrencias(1);
        return monitorAlertaRepository.save(nueva);
    }

    /**
     * "Relacionar las alertas" (1/2): agrupa las alertas de ESTE ciclo por componente. Dos o mas
     * alertas simultaneas del mismo componente son mas probables de compartir una causa raiz que
     * alertas de componentes distintos, asi que se marcan como "posible causa comun" en el
     * dashboard en vez de mostrarse como sintomas independientes.
     */
    private List<GrupoAlertasView> agruparAlertas(List<MonitorAlerta> alertas) {
        Map<String, List<MonitorAlerta>> porComponente = new LinkedHashMap<>();
        for (MonitorAlerta alerta : alertas) {
            porComponente.computeIfAbsent(alerta.getComponente(), k -> new ArrayList<>()).add(alerta);
        }
        List<GrupoAlertasView> grupos = new ArrayList<>();
        for (Map.Entry<String, List<MonitorAlerta>> entry : porComponente.entrySet()) {
            grupos.add(new GrupoAlertasView(IndicadorOracleUtil.nombreComponente(entry.getKey()),
                    entry.getValue(), entry.getValue().size() >= 2));
        }
        return grupos;
    }

    /** Los ultimos N puntos del ISBD combinado, en orden CRONOLOGICO (mas antiguo primero). */
    public List<MonitorIndice> historial(int ultimosN) {
        List<MonitorIndice> recientes = monitorIndiceRepository.findAllByOrderByFechaHoraDesc(PageRequest.of(0, ultimosN));
        Collections.reverse(recientes);
        return recientes;
    }

    public List<MonitorProcesos> historialProcesos(int ultimosN) {
        List<MonitorProcesos> recientes = monitorProcesosRepository.findAllByOrderByFechaHoraDesc(PageRequest.of(0, ultimosN));
        Collections.reverse(recientes);
        return recientes;
    }

    public List<MonitorMemoria> historialMemoria(int ultimosN) {
        List<MonitorMemoria> recientes = monitorMemoriaRepository.findAllByOrderByFechaHoraDesc(PageRequest.of(0, ultimosN));
        Collections.reverse(recientes);
        return recientes;
    }

    public List<MonitorArchivos> historialArchivos(int ultimosN) {
        List<MonitorArchivos> recientes = monitorArchivosRepository.findAllByOrderByFechaHoraDesc(PageRequest.of(0, ultimosN));
        Collections.reverse(recientes);
        return recientes;
    }

    /**
     * Historico por tablespace: trae una ventana de filas crudas (todas las tablespaces
     * mezcladas, mas recientes primero), las agrupa por nombre y devuelve cada serie ya en orden
     * cronologico - resuelve directamente "¿los tablespaces se están llenando?" (sección 23).
     */
    public List<HistoricoTablespaceView> historialTablespaces() {
        List<MonitorTablespace> recientes = monitorTablespaceRepository.findAllByOrderByFechaHoraDesc(PageRequest.of(0, 200));
        Collections.reverse(recientes);
        Map<String, List<MonitorTablespace>> porTablespace = new LinkedHashMap<>();
        for (MonitorTablespace fila : recientes) {
            porTablespace.computeIfAbsent(fila.getTablespaceName(), k -> new ArrayList<>()).add(fila);
        }
        List<HistoricoTablespaceView> vista = new ArrayList<>();
        for (Map.Entry<String, List<MonitorTablespace>> entry : porTablespace.entrySet()) {
            vista.add(new HistoricoTablespaceView(entry.getKey(), entry.getValue()));
        }
        return vista;
    }

    private IndicadorComponenteView aComponenteView(String nombre, BigDecimal salud, List<Metrica> metricas) {
        List<MetricaDetalleView> detalle = new ArrayList<>();
        for (Metrica metrica : metricas) {
            detalle.add(metrica.toDetalle());
        }
        EstadoSalud estadoSubindice = IndicadorOracleUtil.estadoIsbd(salud);
        return new IndicadorComponenteView(nombre, salud,
                IndicadorOracleUtil.textoEstadoSalud(estadoSubindice),
                IndicadorOracleUtil.cssEstadoSalud(estadoSubindice), detalle);
    }

    private List<String> construirCausas(List<Metrica> todasMetricas, BigDecimal saludProcesos,
                                          BigDecimal saludMemoria, BigDecimal saludArchivos, EstadoSalud estadoReal) {
        if (estadoReal != EstadoSalud.CRITICO) {
            return List.of();
        }
        List<String> causas = new ArrayList<>();
        agregarSiCritico(causas, "procesos", saludProcesos);
        agregarSiCritico(causas, "memoria", saludMemoria);
        agregarSiCritico(causas, "archivos", saludArchivos);
        for (Metrica metrica : todasMetricas) {
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

    private ResultadoProcesos calcularProcesos() {
        Map<String, OracleVistaDinamicaService.LimiteRecurso> limites = vistaDinamicaService.obtenerLimitesRecursos();
        BigDecimal procUtil = pct(limites.get("processes"));
        BigDecimal sessUtil = pct(limites.get("sessions"));
        long bloqueadas = vistaDinamicaService.contarSesionesBloqueadas();
        long longops = vistaDinamicaService.contarOperacionesProlongadas();

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
        // V$SESSION_LONGOPS (seccion 6.2/6.3 de la guia): operaciones que requieren tiempo
        // considerable. No son necesariamente un problema (backups, rebuilds), por eso se
        // clasifican con umbrales mas tolerantes que sesiones bloqueadas, igual que over-allocation.
        metricas.add(new Metrica("PROCESOS", "Operaciones prolongadas", longops + " operación(es)", "3 / 10",
                claseConteo(longops, 3, 10),
                longops > 0
                        ? "Hay " + longops + " operación(es) de larga duración en curso (V$SESSION_LONGOPS)."
                        : "No hay operaciones de larga duración en curso."));

        BigDecimal scoreProc = IndicadorOracleUtil.invertirUtilizacion(procUtil);
        BigDecimal scoreSess = IndicadorOracleUtil.invertirUtilizacion(sessUtil);
        BigDecimal scoreBloqueadas = bloqueadas == 0
                ? BigDecimal.valueOf(100)
                : IndicadorOracleUtil.clamp(BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(bloqueadas * 20)));
        BigDecimal scoreLongops = longops == 0
                ? BigDecimal.valueOf(100)
                : IndicadorOracleUtil.clamp(BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(longops * 15)));

        BigDecimal ip = IndicadorOracleUtil.clamp(
                PESO_PROC_UTIL.multiply(scoreProc)
                        .add(PESO_SESS_UTIL.multiply(scoreSess))
                        .add(PESO_BLOQUEADAS.multiply(scoreBloqueadas))
                        .add(PESO_LONGOPS.multiply(scoreLongops)));

        return new ResultadoProcesos(ip, metricas, procUtil, sessUtil, bloqueadas, longops);
    }

    private BigDecimal pct(OracleVistaDinamicaService.LimiteRecurso limite) {
        return limite == null ? BigDecimal.ZERO : limite.getPorcentajeUtilizacion();
    }

    // ---- Indicador de Memoria (IM) ----

    private ResultadoMemoria calcularMemoria() {
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

        return new ResultadoMemoria(im, metricas, pgaHit, overAlloc, sgaUsedPct);
    }

    // ---- Indicador de Archivos (IA) ----

    private ResultadoArchivos calcularArchivos() {
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

        return new ResultadoArchivos(ia, metricas, onlineDf, totalDf, onlineTf, totalTf, redologsInvalidos, tablespaces);
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
