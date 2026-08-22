package com.example.auditoriabd.service.monitor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unico punto del proyecto que consulta directamente las vistas dinamicas y
 * del diccionario de datos de Oracle (V$..., DBA_...) para el Monitor de
 * Salud. Se usa {@link JdbcTemplate} en vez de JPA/repositorios porque estas
 * vistas son dinamicas, sin clave primaria estable, y no encajan en el
 * patron entidad/repositorio del resto del proyecto. Reutiliza el mismo
 * DataSource autoconfigurado del datasource principal de la aplicacion (no
 * hay un segundo datasource) - el usuario "auditoria" ya tiene
 * SELECT_CATALOG_ROLE para poder leerlas.
 */
@Service
public class OracleVistaDinamicaService {

    private final JdbcTemplate jdbcTemplate;

    public OracleVistaDinamicaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Un recurso limitado de Oracle (procesos, sesiones): uso actual y limite configurado (null = ilimitado). */
    public static class LimiteRecurso {
        private final long actual;
        private final Long limite;

        public LimiteRecurso(long actual, Long limite) {
            this.actual = actual;
            this.limite = limite;
        }

        public long getActual() {
            return actual;
        }

        public Long getLimite() {
            return limite;
        }

        /** Porcentaje de utilizacion 0-100, o 0 si el recurso es ilimitado (sin presion posible). */
        public BigDecimal getPorcentajeUtilizacion() {
            if (limite == null || limite <= 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(actual)
                    .divide(BigDecimal.valueOf(limite), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }

    /** Uso de un tablespace: porcentaje usado, ya calculado por Oracle. */
    public static class UsoTablespace {
        private final String nombre;
        private final BigDecimal porcentajeUsado;

        public UsoTablespace(String nombre, BigDecimal porcentajeUsado) {
            this.nombre = nombre;
            this.porcentajeUsado = porcentajeUsado;
        }

        public String getNombre() {
            return nombre;
        }

        public BigDecimal getPorcentajeUsado() {
            return porcentajeUsado;
        }
    }

    /**
     * Limites de procesos y sesiones, claves 'processes' y 'sessions'.
     *
     * <p>NO se usa V$RESOURCE_LIMIT: esa vista es un concepto de instancia/CDB
     * y, comprobado contra la base real, devuelve 0 filas para una sesion
     * conectada directamente a una PDB (incluso con SELECT_CATALOG_ROLE y
     * usando CONTAINERS()) - no es un problema de permisos, simplemente esta
     * vacia en ese contexto. En su lugar se cuenta el uso real desde
     * V$PROCESS/V$SESSION (ambas si son visibles desde la PDB) y el limite
     * configurado desde V$PARAMETER, que tambien es visible por PDB.
     */
    public Map<String, LimiteRecurso> obtenerLimitesRecursos() {
        Map<String, LimiteRecurso> resultado = new HashMap<>();

        Long procesosActuales = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM v$process", Long.class);
        Long sesionesActuales = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM v$session", Long.class);

        Map<String, Long> limites = new HashMap<>();
        List<Map<String, Object>> filas = jdbcTemplate.queryForList(
                "SELECT name, value FROM v$parameter WHERE name IN ('processes','sessions')");
        for (Map<String, Object> fila : filas) {
            String nombre = String.valueOf(fila.get("NAME"));
            limites.put(nombre, Long.valueOf(String.valueOf(fila.get("VALUE")).trim()));
        }

        resultado.put("processes", new LimiteRecurso(
                procesosActuales == null ? 0 : procesosActuales, limites.get("processes")));
        resultado.put("sessions", new LimiteRecurso(
                sesionesActuales == null ? 0 : sesionesActuales, limites.get("sessions")));
        return resultado;
    }

    /** Cantidad de sesiones actualmente bloqueadas esperando por otra sesion (V$SESSION.blocking_session). */
    public long contarSesionesBloqueadas() {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM v$session WHERE blocking_session IS NOT NULL", Long.class);
        return total == null ? 0 : total;
    }

    /**
     * Estadisticas de PGA (V$PGASTAT), claves: "total PGA allocated" (bytes),
     * "total PGA inuse" (bytes), "over allocation count", "cache hit percentage".
     */
    public Map<String, BigDecimal> obtenerEstadisticasPga() {
        return jdbcTemplate.query("SELECT name, value FROM v$pgastat WHERE name IN " +
                        "('total PGA allocated','total PGA inuse','over allocation count','cache hit percentage')",
                rs -> {
                    Map<String, BigDecimal> mapa = new LinkedHashMap<>();
                    while (rs.next()) {
                        mapa.put(rs.getString("name"), rs.getBigDecimal("value"));
                    }
                    return mapa;
                });
    }

    /**
     * Tamanos de SGA en bytes (V$SGAINFO), claves "Maximum SGA Size" y
     * "Free SGA Memory Available". Comprobado contra V$SGAINFO real: NO
     * existe una fila llamada "Total SGA Size" ni "Free SGA Memory" (esos
     * nombres no aparecen en la vista) - los nombres correctos son
     * "Maximum SGA Size" (techo de SGA_MAX_SIZE) y "Free SGA Memory
     * Available" (memoria aun no asignada a ningun componente, disponible
     * para el Automatic Shared Memory Management).
     */
    public Map<String, BigDecimal> obtenerInfoSga() {
        return jdbcTemplate.query("SELECT name, bytes FROM v$sgainfo WHERE name IN " +
                        "('Maximum SGA Size','Free SGA Memory Available')",
                rs -> {
                    Map<String, BigDecimal> mapa = new LinkedHashMap<>();
                    while (rs.next()) {
                        mapa.put(rs.getString("name"), rs.getBigDecimal("bytes"));
                    }
                    return mapa;
                });
    }

    /**
     * Conteo de datafiles por estado (V$DATAFILE.status), ej. {"ONLINE": 3,
     * "SYSTEM": 1}. Nota: el datafile del tablespace SYSTEM siempre reporta
     * status='SYSTEM', nunca 'ONLINE' - es un estado sano, no un problema
     * (comprobado contra V$DATAFILE real). El llamador debe tratar
     * 'SYSTEM' igual que 'ONLINE'.
     */
    public Map<String, Long> obtenerEstadoDatafiles() {
        return contarPorEstado("SELECT status, COUNT(*) cnt FROM v$datafile GROUP BY status");
    }

    /** Conteo de tempfiles por estado (V$TEMPFILE.status). */
    public Map<String, Long> obtenerEstadoTempfiles() {
        return contarPorEstado("SELECT status, COUNT(*) cnt FROM v$tempfile GROUP BY status");
    }

    /**
     * Conteo de miembros de redo log por estado (V$LOGFILE.status): un status
     * en blanco significa miembro sano, se normaliza a 'OK'.
     */
    public Map<String, Long> obtenerEstadoRedoLogs() {
        return contarPorEstado("SELECT NVL(status,'OK') status, COUNT(*) cnt FROM v$logfile GROUP BY NVL(status,'OK')");
    }

    private Map<String, Long> contarPorEstado(String sql) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        List<Map<String, Object>> filas = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> fila : filas) {
            String estado = String.valueOf(fila.get("STATUS"));
            long cnt = ((Number) fila.get("CNT")).longValue();
            mapa.put(estado, cnt);
        }
        return mapa;
    }

    /**
     * Uso de espacio por tablespace, en porcentaje.
     *
     * <p>NO se calcula a mano desde DBA_DATA_FILES/DBA_FREE_SPACE: esa cuenta
     * usa el tamano ACTUAL de los datafiles como denominador, pero en esta
     * base (y en general con AUTOEXTEND ON) el tamano actual es mucho menor
     * que MAXBYTES - comprobado contra la base real, esto sobreestimaba
     * brutalmente el uso (ej. SYSTEM salia en 99% cuando el uso real,
     * contando el techo de autoextension, es 0.82%). DBA_TABLESPACE_USAGE_METRICS
     * es la vista que Oracle mantiene para esto exactamente y ya usa el
     * tamano efectivo (con autoextension) como denominador, asi que se
     * consulta directo - es la misma vista/columna recomendada por Oracle
     * para monitoreo de espacio de tablespaces. Se excluyen los tablespaces
     * temporales (ya se miden aparte via V$TEMPFILE).
     */
    public List<UsoTablespace> obtenerUsoTablespaces() {
        return jdbcTemplate.query(
                "SELECT m.tablespace_name AS nombre, m.used_percent AS used_percent " +
                        "FROM dba_tablespace_usage_metrics m " +
                        "JOIN dba_tablespaces t ON t.tablespace_name = m.tablespace_name " +
                        "WHERE t.contents != 'TEMPORARY'",
                (rs, rowNum) -> new UsoTablespace(rs.getString("nombre"), rs.getBigDecimal("used_percent")));
    }
}
