package rl.sage.rangerlevels.items.gemas;

import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mantenimiento de las Gemas de Experiencia activas:
 *  - Para cada jugador (UUID) guardamos qué bonus (%) tiene y hasta qué timestamp dura.
 */
public class ExpGemHandler {

    /** Estructura interna que guarda cuánto bonus y hasta cuándo. */
    private static class GemData {
        private final double bonusMultiplier; // por ejemplo 0.10 para 10%, 0.30 para 30%, etc.
        private final long expiresAtMs;       // System.currentTimeMillis() + duración

        private GemData(double bonusMultiplier, long expiresAtMs) {
            this.bonusMultiplier = bonusMultiplier;
            this.expiresAtMs = expiresAtMs;
        }
    }

    /** Mapa que asocia jugador UUID → GemData activa */
    private static final Map<UUID, GemData> ACTIVE_GEMS = new HashMap<>();

    /**
     * Activa una gema para el jugador dado, con X porcentaje de bonus y duración en milisegundos.
     * Si ya tenía una gema activa, la sobrescribe si la nueva expira más tarde.
     *
     * @param player          Jugador al que activar la gema
     * @param bonusMultiplier Porcentaje extra, expresado como decimal (ej. 0.10, 0.30, 0.50)
     * @param durationMs      Duración de la gema en milisegundos
     */
    public static void activarGem(ServerPlayerEntity player, double bonusMultiplier, long durationMs) {
        UUID uuid = player.getUUID();
        long now = System.currentTimeMillis();
        long newExpires = now + durationMs;

        GemData existing = ACTIVE_GEMS.get(uuid);
        if (existing != null) {
            // Si ya tenía una gema activa y la nueva dura más tiempo, la sobrescribimos.
            if (newExpires > existing.expiresAtMs) {
                ACTIVE_GEMS.put(uuid, new GemData(bonusMultiplier, newExpires));
            }
        } else {
            ACTIVE_GEMS.put(uuid, new GemData(bonusMultiplier, newExpires));
        }
    }

    /**
     * Devuelve el bonus actual para ese jugador (0.0 si no hay gema activa o ya expiró).
     * Si la gema expiró, la remueve del mapa.
     *
     * @param player Jugador a consultar
     * @return Double entre 0.0 y (por ejemplo) 0.50. 0.0 = sin bonus.
     */
    public static double getBonus(ServerPlayerEntity player) {
        UUID uuid = player.getUUID();
        GemData data = ACTIVE_GEMS.get(uuid);
        if (data == null) return 0.0;

        long now = System.currentTimeMillis();
        if (now >= data.expiresAtMs) {
            // Ya expiró → removemos y devolvemos 0
            ACTIVE_GEMS.remove(uuid);
            return 0.0;
        }
        return data.bonusMultiplier;
    }

    /**
     * (Opcional) Remueve manualmente la gema activa de un jugador, si se quiere invalidar antes.
     */
    public static void limpiarGem(ServerPlayerEntity player) {
        ACTIVE_GEMS.remove(player.getUUID());
    }
}
