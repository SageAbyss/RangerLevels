package rl.sage.rangerlevels.multiplier;

import net.minecraft.entity.player.PlayerEntity;

import java.time.Instant;

/**
 * API para leer/ajustar multiplicadores con temporizadores.
 */
public class MultiplierManager {
    private static final MultiplierManager INST = new MultiplierManager();
    private MultiplierState state;

    private MultiplierManager() {
        // Cargamos el estado persistido (multipliers.yml)
        this.state = MultiplierState.load();
    }

    /** Obtiene la instancia singleton */
    public static MultiplierManager instance() {
        return INST;
    }

    /** Fuerza recarga desde disco */
    public void reload() {
        this.state = MultiplierState.load();
    }

    /**
     * Devuelve el multiplicador global actual (1.0 si expiró).
     */
    public double getGlobal() {
        long now = Instant.now().getEpochSecond();
        if (state.globalExpiry > 0 && now > state.globalExpiry) {
            // Expiró → reseteamos automáticamente
            state.globalMultiplier = 1.0;
            state.globalExpiry     = -1;
            state.save();
            return 1.0;
        }
        return state.globalMultiplier;
    }

    /**
     * Devuelve el multip. privado de un jugador (1.0 si expiró o no existe).
     */
    public double getPlayer(PlayerEntity player) {
        String name = player.getName().getString();
        MultiplierState.PlayerEntry entry = state.playerMultipliers.get(name);
        if (entry == null) return 1.0;

        long now = Instant.now().getEpochSecond();
        if (entry.expiry > 0 && now > entry.expiry) {
            // Expiró → lo quitamos
            state.playerMultipliers.remove(name);
            state.save();
            return 1.0;
        }
        return entry.value;
    }

    /**
     * Establece un multiplicador global por un periodo (en segundos).
     * Si seconds ≤ 0 → indefinido.
     */
    public void setGlobal(double value, long seconds) {
        long expiry = seconds > 0
                ? Instant.now().getEpochSecond() + seconds
                : -1;
        state.globalMultiplier = value;
        state.globalExpiry     = expiry;
        state.save();
    }

    /**
     * Establece un multiplicador privado para un jugador por un periodo.
     * Si seconds ≤ 0 → indefinido.
     */
    public void setPlayer(String playerName, double value, long seconds) {
        long expiry = seconds > 0
                ? Instant.now().getEpochSecond() + seconds
                : -1;
        state.setPlayer(playerName, value, expiry);
        state.save();
    }

    /**
     * Segundos restantes del multiplicador global.
     * @return ≥0 si hay timer, o -1 si indefinido / ya expiró.
     */
    public long getGlobalRemainingSeconds() {
        long now = Instant.now().getEpochSecond();
        if (state.globalExpiry < 0) return -1;
        long rem = state.globalExpiry - now;
        return rem > 0 ? rem : 0;
    }

    /**
     * Segundos restantes del multiplicador privado de un jugador.
     * @return ≥0 si hay timer, o -1 si indefinido o no existe.
     */
    public long getPlayerRemainingSeconds(PlayerEntity player) {
        String name = player.getName().getString();
        MultiplierState.PlayerEntry entry = state.playerMultipliers.get(name);
        if (entry == null || entry.expiry < 0) return -1;
        long now = Instant.now().getEpochSecond();
        long rem = entry.expiry - now;
        return rem > 0 ? rem : 0;
    }
    /**
     * Suma un valor al multiplicador actual de un jugador.
     * Si no tiene uno activo, se comporta como setPlayer.
     * Si seconds > 0, la expiración será la más lejana entre la actual y la nueva.
     */
    public void addPlayerMultiplier(String playerName, double value, long seconds) {
        MultiplierState.PlayerEntry current = state.playerMultipliers.get(playerName);
        long now = Instant.now().getEpochSecond();

        double newValue = value;
        long newExpiry = seconds > 0 ? now + seconds : -1;

        if (current != null) {
            if (current.expiry > 0 && now > current.expiry) {
                // Expirado → tratamos como nuevo
                current = null;
            }
        }

        if (current != null) {
            newValue += current.value;
            if (current.expiry > 0 && newExpiry > 0)
                newExpiry = Math.max(current.expiry, newExpiry);
            else if (current.expiry > 0)
                newExpiry = current.expiry;
        }

        state.setPlayer(playerName, newValue, newExpiry);
        state.save();
    }
    /**
     * Suma un valor al multiplicador global actual.
     * Si no hay ninguno activo, se comporta como setGlobal.
     * Si seconds > 0, la expiración será la más lejana entre la actual y la nueva.
     */
    public void addGlobal(double value, long seconds) {
        long now = Instant.now().getEpochSecond();
        double currentVal   = getGlobal();
        long   currentExp   = state.globalExpiry;

        double newVal = currentVal + value;
        long   newExp = seconds > 0
                ? now + seconds
                : -1;

        if (currentExp > 0 && newExp > 0) {
            newExp = Math.max(currentExp, newExp);
        } else if (currentExp > 0) {
            newExp = currentExp;
        }

        state.globalMultiplier = newVal;
        state.globalExpiry     = newExp;
        state.save();
    }

}
