package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Implementación de IPassCapability para uso en servidor, ahora con expiración
 * y restauración automática del pase anterior cuando un pase temporal expira.
 */
public class PassCapability implements IPassCapability, INBTSerializable<CompoundNBT> {
    /** Pase activo actual: 0=Free, 1=Super, 2=Ultra, 3=Master */
    private int tier = 0;
    /** Timestamp UTC en ms cuando expira; Long.MAX_VALUE = nunca (permanente) */
    private long expiresAt = 0L;

    /** Pase anterior (para restaurar tras expiración) */
    private int previousTier = 0;
    private long previousExpiresAt = 0L;

    public PassCapability() {
        // Por defecto, tier=0 (Free), expiresAt=0, sin pase anterior
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
    public int getPreviousTier() {
        return previousTier;
    }

    @Override
    public void setPreviousTier(int previousTier) {
        this.previousTier = previousTier;
    }

    @Override
    public long getPreviousExpiresAt() {
        return previousExpiresAt;
    }

    @Override
    public void setPreviousExpiresAt(long previousExpiresAt) {
        this.previousExpiresAt = previousExpiresAt;
    }

    @Override
    public void syncToClient(ServerPlayerEntity player) {
        // Si en el futuro quieres notificar al cliente, iría aquí.
    }

    @Override
    public boolean hasActivePass() {
        long now = System.currentTimeMillis();

        // Si no hay pase (tier <= 0) → Free
        if (tier <= 0) {
            return false;
        }

        // Si es permanente (expiresAt == Long.MAX_VALUE) → sigue activo
        if (expiresAt == Long.MAX_VALUE) {
            return true;
        }

        // Si no ha expirado aún → sigue activo
        if (now < expiresAt) {
            return true;
        }

        // Si llegó aquí, el pase temporal sí expiró.
        // Restauramos el pase anterior si existía:
        if (previousTier > 0) {
            int restoredTier = previousTier;
            long restoredExpires = previousExpiresAt;

            // Limpiamos el “pase temporal” actual
            this.tier = restoredTier;
            this.expiresAt = restoredExpires;

            // Limpiar la información anterior (ahora ya restaurada)
            this.previousTier = 0;
            this.previousExpiresAt = 0L;
            return (restoredTier > 0);
        }

        // Si no había pase anterior, se queda en Free
        this.tier = 0;
        this.expiresAt = 0L;
        return false;
    }

    @Override
    public void grantPass(int newTier, long durationMillis) {
        long now = System.currentTimeMillis();
        long newExpiresAt = (durationMillis <= 0 ? Long.MAX_VALUE : now + durationMillis);

        // Antes de sobrescribir, guardamos el pase actual COMO “anterior”
        // Solo si había pase activo que no sea Free
        if (this.tier > 0) {
            this.previousTier = this.tier;
            this.previousExpiresAt = this.expiresAt;
        } else {
            // Si no tenía pase (Free), no hay “pase anterior”
            this.previousTier = 0;
            this.previousExpiresAt = 0L;
        }

        // Ahora asignamos el pase nuevo:
        this.tier = newTier;
        this.expiresAt = newExpiresAt;
    }

    /** Serializa TODO a NBT (incluye pase actual y pase anterior) */
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        // Pase activo
        nbt.putInt("PassTier", tier);
        nbt.putLong("PassExpiresAt", expiresAt);
        // Pase anterior
        nbt.putInt("PreviousPassTier", previousTier);
        nbt.putLong("PreviousPassExpiresAt", previousExpiresAt);
        return nbt;
    }

    /** Restaura TODO desde NBT */
    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("PassTier")) {
            this.tier = nbt.getInt("PassTier");
        }
        if (nbt.contains("PassExpiresAt")) {
            this.expiresAt = nbt.getLong("PassExpiresAt");
        }
        if (nbt.contains("PreviousPassTier")) {
            this.previousTier = nbt.getInt("PreviousPassTier");
        }
        if (nbt.contains("PreviousPassExpiresAt")) {
            this.previousExpiresAt = nbt.getLong("PreviousPassExpiresAt");
        }
    }
}
