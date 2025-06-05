package rl.sage.rangerlevels.items.gemas;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;

/**
 * Mantenimiento de las Gemas de Experiencia activas:
 *  - Ahora se almacena en el NBT persistente de cada jugador (player.getPersistentData()).
 *  - Claves usadas en el NBT:
 *      "RangerGem_Bonus"      → double (bonus actual, ej. 0.10, 0.30, 0.50)
 *      "RangerGem_ExpiresAt"  → long   (timestamp de expiración en millis)
 */
public class ExpGemHandler {

    private static final String NBT_KEY = "RangerGem";
    private static final String NBT_BONUS      = "RangerGem_Bonus";
    private static final String NBT_EXPIRES_AT = "RangerGem_ExpiresAt";

    /**
     * Activa una gema para el jugador dado, con X porcentaje de bonus y duración en milisegundos.
     * Si ya tenía una gema activa en NBT y la nueva expira más tarde, la sobrescribe.
     *
     * @param player        Jugador al que activar la gema
     * @param bonusMultiplier  porcentaje extra  (ej. 0.10, 0.30, 0.50)
     * @param durationMs    duración de la gema en ms
     */
    public static void activarGem(ServerPlayerEntity player, double bonusMultiplier, long durationMs) {
        CompoundNBT persist = player.getPersistentData();

        long now = System.currentTimeMillis();
        long newExpiresAt = now + durationMs;

        double existingBonus = getBonus(player);           // lee NBT
        long   existingExpires = getExpiresAt(player);     // lee NBT

        // Si hay una gema activa y expira más tarde, no sobrescribimos.
        if (existingBonus > 0.0 && existingExpires >= newExpiresAt) {
            return;
        }

        // Guardamos en NBT:
        CompoundNBT gemTag = new CompoundNBT();
        gemTag.putDouble(NBT_BONUS, bonusMultiplier);
        gemTag.putLong(NBT_EXPIRES_AT, newExpiresAt);
        persist.put(NBT_KEY, gemTag);

        // Sincronizar con cliente si fuese necesario (dependerá de cómo envíes el NBT al cliente)
        // Por ejemplo: Packets para sincronizar persistentData.
    }

    /**
     * Devuelve el bonus actual para ese jugador (0.0 si no hay gema activa o ya expiró).
     * Si la gema expiró, limpia las claves en NBT y devuelve 0.
     */
    public static double getBonus(ServerPlayerEntity player) {
        CompoundNBT persist = player.getPersistentData();
        if (!persist.contains(NBT_KEY)) return 0.0;

        CompoundNBT gemTag = persist.getCompound(NBT_KEY);
        if (!gemTag.contains(NBT_BONUS) || !gemTag.contains(NBT_EXPIRES_AT)) {
            persist.remove(NBT_KEY);
            return 0.0;
        }

        long expiresAt = gemTag.getLong(NBT_EXPIRES_AT);
        long now = System.currentTimeMillis();
        if (now >= expiresAt) {
            // expiró → limpiamos NBT
            persist.remove(NBT_KEY);
            return 0.0;
        }

        return gemTag.getDouble(NBT_BONUS);
    }

    /**
     * Devuelve el tiempo restante en milisegundos de la gema activa para el jugador (0 si no hay).
     */
    public static long getRemainingDurationMs(ServerPlayerEntity player) {
        CompoundNBT persist = player.getPersistentData();
        if (!persist.contains(NBT_KEY)) return 0L;

        CompoundNBT gemTag = persist.getCompound(NBT_KEY);
        if (!gemTag.contains(NBT_EXPIRES_AT)) {
            persist.remove(NBT_KEY);
            return 0L;
        }

        long expiresAt = gemTag.getLong(NBT_EXPIRES_AT);
        long now = System.currentTimeMillis();
        long remaining = expiresAt - now;
        if (remaining <= 0L) {
            persist.remove(NBT_KEY);
            return 0L;
        }
        return remaining;
    }

    /**
     * (Opcional) Devuelve el timestamp de expiración, o 0 si no hay gema.
     */
    public static long getExpiresAt(ServerPlayerEntity player) {
        CompoundNBT persist = player.getPersistentData();
        if (!persist.contains(NBT_KEY)) return 0L;
        CompoundNBT gemTag = persist.getCompound(NBT_KEY);
        return gemTag.getLong(NBT_EXPIRES_AT);
    }

    /**
     * (Opcional) Forzar limpieza de la gema activa (por ejemplo, en algún comando).
     */
    public static void limpiarGem(ServerPlayerEntity player) {
        player.getPersistentData().remove(NBT_KEY);
    }
}
