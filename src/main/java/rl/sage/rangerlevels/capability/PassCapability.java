package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Implementación de IPassCapability para uso en servidor, ahora con expiración.
 */
public class PassCapability implements IPassCapability, INBTSerializable<CompoundNBT> {
    /** Nivel de pase: 0=sin pase, 1=Super, 2=Ultra, 3=Master */
    private int tier = 0;

    /** Timestamp en milisegundos (UTC) cuando expira el pase; 0 si no hay pase. */
    private long expiresAt = 0L;

    public PassCapability() {
        // Por defecto, tier=0, expiresAt=0 (sin pase).
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public void setTier(int tier) {
        this.tier = tier;
    }

    @Override
    public long getExpiresAt() {
        return expiresAt;
    }

    @Override
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public void syncToClient(ServerPlayerEntity player) {
        // No-op para server-only. Si más adelante deseas notificar cambio de pase a cliente,
        // enviarías un packet aquí.
    }

    @Override
    public boolean hasActivePass() {
        // Si tier == 0, no hay pase
        if (tier <= 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < expiresAt) {
            return true;
        }
        // Si ya expiró, "limpiamos" la capability y devolvemos false
        tier = 0;
        expiresAt = 0L;
        return false;
    }

    @Override
    public void grantPass(int tier, long durationMillis) {
        this.tier = tier;
        this.expiresAt = System.currentTimeMillis() + durationMillis;
    }

    /** Serialización NBT para guardar en disco */
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("PassTier", tier);
        nbt.putLong("PassExpiresAt", expiresAt);
        return nbt;
    }

    /** Lectura de NBT cuando se carga el jugador */
    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("PassTier")) {
            this.tier = nbt.getInt("PassTier");
        }
        if (nbt.contains("PassExpiresAt")) {
            this.expiresAt = nbt.getLong("PassExpiresAt");
        }
    }
}
