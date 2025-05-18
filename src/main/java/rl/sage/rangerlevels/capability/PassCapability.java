package rl.sage.rangerlevels.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Implementación por defecto de IPassCapability para uso únicamente en servidor.
 */
public class PassCapability implements IPassCapability, INBTSerializable<CompoundNBT> {
    /** Nivel del pase */
    private int tier = 0;

    public PassCapability() {
        // tier por defecto ya es 0
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public void setTier(int tier) {
        this.tier = tier;
    }

    /**
     * En modo server-only no enviamos nada al cliente,
     * así que dejamos este método vacío.
     */
    @Override
    public void syncToClient(net.minecraft.entity.player.ServerPlayerEntity player) {
        // No-op: solo server-side
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("PassTier", tier);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("PassTier")) {
            this.tier = nbt.getInt("PassTier");
        }
    }
}
