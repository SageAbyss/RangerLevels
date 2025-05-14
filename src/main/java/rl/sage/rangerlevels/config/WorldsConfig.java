package rl.sage.rangerlevels.config;

import java.util.List;

/**
 * Configuración de control de mundos:
 *
 * worlds.enable    → si está en false, ignora cualquier filtrado y permite EXP en todos los mundos.
 * worlds.whitelist → si está en true, solo permite EXP en los mundos de la lista.
 *                    si estuviera en false (blacklist), bloquearía EXP en los mundos de la lista.
 * worlds.list      → nombres de mundos a incluir (whitelist) o excluir (blacklist).
 */
public class WorldsConfig {
    /** Habilita o deshabilita el filtrado por mundo */
    public boolean enable = false;
    /** true = whitelist; false = blacklist */
    public boolean whitelist = true;
    /** Nombres de mundo (folder name) */
    public List<String> list;

    /**
     * Comprueba si este mundo está permitido para obtención de EXP.
     * @param worldName nombre del mundo (según getWorldName())
     * @return true si permitimos EXP aquí
     */
    public boolean isAllowed(String worldName) {
        // Si no filtramos, todo está permitido
        if (!enable) return true;

        boolean inList = list != null && list.contains(worldName);
        if (whitelist) {
            // solo permitidos los de la lista
            return inList;
        } else {
            // blacklist: bloqueados los de la lista
            return !inList;
        }
    }
}
