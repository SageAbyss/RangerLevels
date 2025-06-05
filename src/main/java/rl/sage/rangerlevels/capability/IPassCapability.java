package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface IPassCapability {
    /** Devuelve el nivel numérico del pase (0 = sin pase, 1=Super, 2=Ultra, 3=Master). */
    int getTier();

    /** Asigna el nivel del pase (0 para ninguno). */
    void setTier(int tier);

    /** Devuelve el timestamp (ms desde epoch) en que expira el pase. 0 si no hay pase asignado. */
    long getExpiresAt();

    /** Fija el timestamp (ms) de expiración. */
    void setExpiresAt(long expiresAt);

    int getPreviousTier();
    void setPreviousTier(int previousTier);

    long getPreviousExpiresAt();
    void setPreviousExpiresAt(long previousExpiresAt);

    /**
     * Sincroniza la capability en el cliente (si implementaras algo en client-side).
     * Como tu mod es server-only, puede quedarse vacío o lanzar NoOp.
     */
    void syncToClient(ServerPlayerEntity player);

    /**
     * Shortcut: si tiene Tier > 0 y ahora < expiresAt, retorna true.
     * De lo contrario, setea tier=0 y retorna false.
     */
    boolean hasActivePass();

    /**
     * Conveniencia para “otorgar un pase temporal”:
     * - Asigna el tier
     * - Calcula expiresAt = ahora + durationMillis
     * - (Opcional) invoca syncToClient
     */
    void grantPass(int tier, long durationMillis);
}
