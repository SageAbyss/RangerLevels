package rl.sage.rangerlevels.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsea duraciones como "30s", "5m", "2h", "2d" a segundos totales,
 * y formatea duraciones en segundos a cadenas legibles.
 */
public class TimeUtil {
    // Permite capturar un número seguido opcionalmente de un sufijo (s|m|h|d),
    // con posibles espacios alrededor.
    private static final Pattern DURATION = Pattern.compile("^\\s*(\\d+)\\s*([smhdSMHD])?\\s*$");

    /**
     * Convierte una cadena con sufijo:
     *  - 's' (segundos),
     *  - 'm' (minutos),
     *  - 'h' (horas),
     *  - 'd' (días),
     * o sin sufijo (se asume segundos),
     * en el total de segundos.
     *
     * @param s duración con sufijo, p.ej. "30s", " 5 m ", "2H", "1d", o "120"
     * @return total de segundos
     * @throws IllegalArgumentException si es inválido
     */
    public static long parseDuration(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Cadena vacía");
        }
        Matcher m = DURATION.matcher(s);
        if (!m.matches()) {
            throw new IllegalArgumentException("Formato inválido de duración: '" + s + "'");
        }

        long value = Long.parseLong(m.group(1));
        String suf = m.group(2);
        if (suf == null) {
            // sin sufijo → segundos
            return value;
        }

        switch (Character.toLowerCase(suf.charAt(0))) {
            case 's': return value;
            case 'm': return value * 60;
            case 'h': return value * 3600;
            case 'd': return value * 86400;
            default:
                // Nunca debería llegar aquí
                throw new IllegalArgumentException("Sufijo inválido: " + suf);
        }
    }

    // -------- el método formatDuration se mantiene igual --------
    public static String formatDuration(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0s";
        }
        long days    = totalSeconds / 86400;
        long rem1    = totalSeconds % 86400;
        long hours   = rem1 / 3600;
        long rem2    = rem1 % 3600;
        long minutes = rem2 / 60;
        long seconds = rem2 % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0)    sb.append(days).append("d");
        if (hours > 0)   sb.append(hours).append("h");
        if (minutes > 0) sb.append(minutes).append("m");
        sb.append(seconds).append("s");
        return sb.toString();
    }
}
