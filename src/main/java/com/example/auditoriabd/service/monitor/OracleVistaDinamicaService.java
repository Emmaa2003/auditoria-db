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

    /** Uso de un tablespace: bytes totales y bytes libres. */
    public static class UsoTablespace {
        private final String nombre;
        private final long bytesTotal;
        private final long bytesLibres;

        public UsoTablespace(String nombre, long bytesTotal, long bytesLibres) {
            this.nombre = nombre;
            this.bytesTotal = bytesTotal;
            this.bytesLibres = bytesLibres;
        }

        public String getNombre() {
            return nombre;
        }

        public BigDecimal getPorcentajeUsado() {
            if (bytesTotal <= 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(bytesTotal - bytesLibres)
                    .divide(BigDecimal.valueOf(bytesTotal), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }

    /** Limites de procesos y sesiones (V$RESOURCE_LIMIT), claves 'processes' y 'sessions'. */
    public Map<String, LimiteRecurso> obtenerLimitesRecursos() {
        Map<String, LimiteRecurso> resultado = new HashMap<>();
        List<Map<String, Object>> filas = jdbcTemplate.queryForList(
                "SELECT resource_name, current_utilization, limit_value " +
                        "FROM v$resource_limit WHERE resource_name IN ('processes','sessions')");
        for (Map<String, Object> fila : filas) {
            String nombre = String.valueOf(fila.get("RESOURCE_NAME"));
            long actual = ((Number) fila.get("CURRENT_UTILIZATION")).longValue();
            String limiteStr = String.valueOf(fila.get("LIMIT_VALUE"));
            Long limite = "UNLIMITED".equalsIgnoreCase(limiteStr) ? null : Long.valueOf(limiteStr.trim());
            resultado.put(nombre, new LimiteRecurso(actual, limite));
        }
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

    /** Tamanos de SGA en bytes (V$SGAINFO), claves "Total SGA Size" y "Free SGA Memory". */
    public Map<String, BigDecimal> obtenerInfoSga() {
        return jdbcTemplate.query("SELECT name, bytes FROM v$sgainfo WHERE name IN " +
                        "('Total SGA Size','Free SGA Memory')",
                rs -> {
                    Map<String, BigDecimal> mapa = new LinkedHashMap<>();
                    while (rs.next()) {
                        mapa.put(rs.getString("name"), rs.getBigDecimal("bytes"));
                    }
                    return mapa;
                });
    }

    /** Conteo de datafiles por estado (V$DATAFILE.status), ej. {"ONLINE": 5, "OFFLINE": 0}. */
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

    /** Uso de espacio por tablespace (DBA_DATA_FILES + DBA_FREE_SPACE). */
    public List<UsoTablespace> obtenerUsoTablespaces() {
        return jdbcTemplate.query(
                "SELECT df.tablespace_name AS nombre, df.total_bytes AS total_bytes, " +
                        "NVL(fs.free_bytes,0) AS free_bytes " +
                        "FROM (SELECT tablespace_name, SUM(bytes) total_bytes FROM dba_data_files GROUP BY tablespace_name) df " +
                        "LEFT JOIN (SELECT tablespace_name, SUM(bytes) free_bytes FROM dba_free_space GROUP BY tablespace_name) fs " +
                        "ON df.tablespace_name = fs.tablespace_name",
                (rs, rowNum) -> new UsoTablespace(
                        rs.getString("nombre"), rs.getLong("total_bytes"), rs.getLong("free_bytes")));
    }
}
