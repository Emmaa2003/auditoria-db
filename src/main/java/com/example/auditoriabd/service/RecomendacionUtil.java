package com.example.auditoriabd.service;

/**
 * Genera texto de recomendación fijo según el código de control ISO/IEC 27002,
 * a partir de los controles con menor madurez del reporte (ver ReporteService).
 */
public final class RecomendacionUtil {

    private RecomendacionUtil() {
    }

    public static String recomendacionPara(String codigoControl) {
        switch (codigoControl) {
            case "A.8.2":
                return "Se recomienda formalizar y revisar periódicamente la lista de usuarios con acceso privilegiado a la base de datos.";
            case "A.8.3":
                return "Se recomienda definir roles de acceso claros para que cada usuario acceda solo a los datos que necesita.";
            case "A.8.5":
                return "Se recomienda reforzar las políticas de autenticación, incluyendo reglas de complejidad de contraseñas y doble factor en accesos críticos.";
            case "A.8.13":
                return "Se recomienda establecer un calendario de copias de seguridad y probar periódicamente su restauración.";
            case "A.8.15":
                return "Se recomienda implementar revisión periódica de los registros de actividad y alertas ante comportamiento sospechoso.";
            case "A.8.24":
                return "Se recomienda ampliar el uso de cifrado a todos los datos sensibles, tanto almacenados como en tránsito.";
            case "A.8.8":
                return "Se recomienda establecer un proceso formal de actualización y revisión de vulnerabilidades del motor de base de datos.";
            case "A.5.15":
                return "Se recomienda documentar y formalizar el procedimiento de solicitud y aprobación de accesos.";
            case "A.6.2":
                return "Se recomienda separar las funciones de solicitud, aprobación y otorgamiento de accesos entre distintas personas.";
            case "A.7.4":
                return "Se recomienda reforzar las medidas de seguridad física del servidor donde reside la base de datos.";
            default:
                return "Se recomienda revisar e implementar mejoras en el control " + codigoControl + ".";
        }
    }
}
