package com.example.auditoriabd.dto.monitor;

import com.example.auditoriabd.entity.monitor.EstadoSalud;
import com.example.auditoriabd.entity.monitor.MonitorAlerta;
import com.example.auditoriabd.entity.monitor.MonitorArchivos;
import com.example.auditoriabd.entity.monitor.MonitorIndice;
import com.example.auditoriabd.entity.monitor.MonitorMemoria;
import com.example.auditoriabd.entity.monitor.MonitorProcesos;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vista completa que consume la pantalla /monitor: el ISBD y su estado
 * "declarado" (segun la escala de la seccion 18), el estado "real" (que puede
 * forzarse a CRITICO por alertas duras, seccion 20), los tres sub-indices con
 * su detalle, las alertas de esta medicion (agrupadas por componente, sección
 * "relacionar las alertas") y el historico reciente para los graficos de
 * evolucion - del ISBD combinado, de cada sub-indice por separado y del %
 * de uso de cada tablespace (sección 23: "¿los tablespaces se están llenando?").
 */
public class MonitorOracleView {

    private BigDecimal isbd;
    private EstadoSalud estadoIsbd;
    private EstadoSalud estadoReal;
    private List<String> causasEstadoReal;
    private IndicadorComponenteView ip;
    private IndicadorComponenteView im;
    private IndicadorComponenteView ia;
    private List<MonitorAlerta> alertasActuales;
    private List<GrupoAlertasView> gruposAlertas;
    private List<MonitorIndice> historico;
    private List<MonitorProcesos> historicoProcesos;
    private List<MonitorMemoria> historicoMemoria;
    private List<MonitorArchivos> historicoArchivos;
    private List<HistoricoTablespaceView> historicoTablespaces;

    public BigDecimal getIsbd() {
        return isbd;
    }

    public void setIsbd(BigDecimal isbd) {
        this.isbd = isbd;
    }

    public EstadoSalud getEstadoIsbd() {
        return estadoIsbd;
    }

    public void setEstadoIsbd(EstadoSalud estadoIsbd) {
        this.estadoIsbd = estadoIsbd;
    }

    public EstadoSalud getEstadoReal() {
        return estadoReal;
    }

    public void setEstadoReal(EstadoSalud estadoReal) {
        this.estadoReal = estadoReal;
    }

    public List<String> getCausasEstadoReal() {
        return causasEstadoReal;
    }

    public void setCausasEstadoReal(List<String> causasEstadoReal) {
        this.causasEstadoReal = causasEstadoReal;
    }

    public IndicadorComponenteView getIp() {
        return ip;
    }

    public void setIp(IndicadorComponenteView ip) {
        this.ip = ip;
    }

    public IndicadorComponenteView getIm() {
        return im;
    }

    public void setIm(IndicadorComponenteView im) {
        this.im = im;
    }

    public IndicadorComponenteView getIa() {
        return ia;
    }

    public void setIa(IndicadorComponenteView ia) {
        this.ia = ia;
    }

    public List<MonitorAlerta> getAlertasActuales() {
        return alertasActuales;
    }

    public void setAlertasActuales(List<MonitorAlerta> alertasActuales) {
        this.alertasActuales = alertasActuales;
    }

    public List<GrupoAlertasView> getGruposAlertas() {
        return gruposAlertas;
    }

    public void setGruposAlertas(List<GrupoAlertasView> gruposAlertas) {
        this.gruposAlertas = gruposAlertas;
    }

    public List<MonitorIndice> getHistorico() {
        return historico;
    }

    public void setHistorico(List<MonitorIndice> historico) {
        this.historico = historico;
    }

    public List<MonitorProcesos> getHistoricoProcesos() {
        return historicoProcesos;
    }

    public void setHistoricoProcesos(List<MonitorProcesos> historicoProcesos) {
        this.historicoProcesos = historicoProcesos;
    }

    public List<MonitorMemoria> getHistoricoMemoria() {
        return historicoMemoria;
    }

    public void setHistoricoMemoria(List<MonitorMemoria> historicoMemoria) {
        this.historicoMemoria = historicoMemoria;
    }

    public List<MonitorArchivos> getHistoricoArchivos() {
        return historicoArchivos;
    }

    public void setHistoricoArchivos(List<MonitorArchivos> historicoArchivos) {
        this.historicoArchivos = historicoArchivos;
    }

    public List<HistoricoTablespaceView> getHistoricoTablespaces() {
        return historicoTablespaces;
    }

    public void setHistoricoTablespaces(List<HistoricoTablespaceView> historicoTablespaces) {
        this.historicoTablespaces = historicoTablespaces;
    }
}
